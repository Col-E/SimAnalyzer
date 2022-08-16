package me.coley.analysis;

import me.coley.analysis.cfg.BlockHandler;
import me.coley.analysis.exception.ResolvableAnalyzerException;
import me.coley.analysis.exception.ResolvableExceptionFactory;
import me.coley.analysis.util.Flow;
import me.coley.analysis.util.FlowUtil;
import me.coley.analysis.util.InternalAnalyzerHackery;
import me.coley.analysis.util.TypeUtil;
import me.coley.analysis.value.AbstractValue;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Analyzer that uses {@link SimFrame} and is based on {@link AbstractValue}s.
 *
 * @author Matt Coley
 */
public class SimAnalyzer extends Analyzer<AbstractValue> {
	private final InternalAnalyzerHackery hackery = new InternalAnalyzerHackery(this);
	private final OpaqueHandler opaqueHandler = new OpaqueHandler(hackery);
	private final SimInterpreter interpreter;
	private final List<Flow> flows = new ArrayList<>();
	private boolean throwUnresolvedAnalyzerErrors = true;
	private boolean skipDeadCodeBlocks = true;
	private MethodNode method;

	/**
	 * Create analyzer.
	 *
	 * @param interpreter
	 * 		Interpreter to use.
	 */
	public SimAnalyzer(SimInterpreter interpreter) {
		super(interpreter);
		this.interpreter = interpreter;
		this.interpreter.setAnalyzer(this);
		this.interpreter.setExceptionFactory(createExceptionFactory());
		this.interpreter.setStaticInvokeFactory(createStaticInvokeFactory());
		this.interpreter.setStaticGetFactory(createStaticGetFactory());
		this.interpreter.setParameterFactory(createParameterFactory());
		this.interpreter.setTypeResolver(createTypeResolver());
		this.interpreter.setTypeChecker(createTypeChecker());
	}

	/**
	 * Called to reset state values between calls to {@link #analyze(String, MethodNode)}.
	 *
	 * @param owner
	 * 		New method owner.
	 * @param method
	 * 		New method to analyze.
	 */
	private void reset(String owner, MethodNode method) {
		this.method = method;
		flows.clear();
		opaqueHandler.reset();
		interpreter.reset(owner, method);
	}

	@Override
	public SimFrame[] analyze(String owner, MethodNode method) throws AnalyzerException {
		reset(owner, method);
		Frame<AbstractValue>[] frames = super.analyze(owner, method);
		SimFrame[] simFrames = copy(frames);
		// Assign frames their instructions
		AbstractInsnNode[] insns = method.instructions.toArray();
		for (int i = 0; i < insns.length; i++) {
			SimFrame frame = simFrames[i];
			if (frame != null)
				frame.setInstruction(insns[i]);
		}
		// Populate recorded control flow
		for (Flow flow : flows) {
			SimFrame from = simFrames[flow.getFrom()];
			SimFrame to = simFrames[flow.getTo()];
			from.flowsInto(to);
		}
		// If the interpreter has problems, check if they've been resolved by checking frames
		if (interpreter.hasReportedProblems()) {
			// Check if the error logged no longer applies given the stack analysis results
			// (due to flow control most likely)
			for (Map.Entry<AbstractInsnNode, AnalyzerException> e :
					new HashSet<>(interpreter.getProblemInsns().entrySet())) {
				if (e.getValue() instanceof ResolvableAnalyzerException) {
					if (((ResolvableAnalyzerException) e.getValue()).validate(method, frames)) {
						interpreter.getProblemInsns().remove(e.getKey());
					}
				}
			}
			// Check one last time
			if (throwUnresolvedAnalyzerErrors && !interpreter.getProblemInsns().isEmpty())
				throw interpreter.getProblemInsns().values().iterator().next();
		}
		return simFrames;
	}

	@Override
	protected SimFrame newFrame(final int numLocals, final int numStack) {
		return new SimFrame(numLocals, numStack);
	}

	@Override
	protected SimFrame newFrame(final Frame<? extends AbstractValue> frame) {
		return new SimFrame((SimFrame) frame);
	}

	@Override
	protected boolean newControlFlowExceptionEdge(int insnIndex, int successorIndex) {
		flows.add(new Flow(insnIndex, successorIndex));
		interpreter.getBlockHandler().add(insnIndex, successorIndex);
		return true;
	}

	@Override
	protected void newControlFlowEdge(int insnIndex, int successorIndex) {
		flows.add(new Flow(insnIndex, successorIndex));
		// Create block when necessary
		if (FlowUtil.isFlowModifier(method, insnIndex, successorIndex)) {
			interpreter.getBlockHandler().add(insnIndex, successorIndex);
		}
		// Modify internal ASM logic to bypass dead code regions
		if (skipDeadCodeBlocks) {
			opaqueHandler.onVisitControlFlowEdge(insnIndex, successorIndex);
		}
	}

	/**
	 * Provides {@link ResolvableExceptionFactory} by default.
	 *
	 * @return Exception factory for interpreter to use.
	 */
	protected ResolvableExceptionFactory createExceptionFactory() {
		return new ResolvableExceptionFactory(createTypeChecker(), getBlockHandler());
	}

	/**
	 * Provides {@code null} by default.
	 *
	 * @return Invoke factory for interpreter to use.
	 */
	protected StaticInvokeFactory createStaticInvokeFactory() {
		return null;
	}

	/**
	 * Provides {@code null} by default.
	 *
	 * @return Getter factory for interpreter to use.
	 */
	protected StaticGetFactory createStaticGetFactory() {
		return null;
	}

	/**
	 * Provides {@code null} by default.
	 *
	 * @return Parameter value factory for interpreter to use.
	 */
	protected ParameterFactory createParameterFactory() {
		return null;
	}

	/**
	 * Provides a basic equality check by default.
	 *
	 * @return Type resolver for interpreter to use.
	 */
	protected TypeResolver createTypeResolver() {
		return new TypeResolver() {
			@Override
			public Type common(Type type1, Type type2) {
				return type1.equals(type2) ? type1 : TypeUtil.OBJECT_TYPE;
			}

			@Override
			public Type commonException(Type type1, Type type2) {
				return type1.equals(type2) ? type1 : TypeUtil.EXCEPTION_TYPE;
			}
		};
	}

	/**
	 * Provides a {@link Class#isAssignableFrom(Class)} comparison by default.
	 * <br>
	 * This is in a lot of cases and is <b>highly recommended</b> that you override this and
	 * provide access to some inheritance graph to support non-runtime types.
	 *
	 * @return Type checker for interpreter to use.
	 */
	protected TypeChecker createTypeChecker() {
		return (parent, child) -> {
			try {
				Class<?> clsParent = Class.forName(parent.getClassName(), false,
						ClassLoader.getSystemClassLoader());
				Class<?> clsChild = Class.forName(child.getClassName(), false,
						ClassLoader.getSystemClassLoader());
				return clsParent.isAssignableFrom(clsChild);
			} catch (Throwable t) {
				return false;
			}
		};
	}

	/**
	 * Determine if resolvable {@link ResolvableAnalyzerException} that go unresolved should be
	 * immediately thrown.
	 * <br>
	 * Default is {@code true}.
	 *
	 * @param throwUnresolvedAnalyzerErrors
	 *        {@code true} to throw unresolved exceptions immediately.
	 *        {@code false} to suppress unresolved exceptions.
	 */
	public void setThrowUnresolvedAnalyzerErrors(boolean throwUnresolvedAnalyzerErrors) {
		this.throwUnresolvedAnalyzerErrors = throwUnresolvedAnalyzerErrors;
	}

	/**
	 * Determine if dead code blocks should be entirely skipped. If enabled,
	 * the resulting {@code frames[i]} where {@code i} is an instruction index in a
	 * dead code block will be {@code null}.
	 * <br>
	 * Default is {@code true}.
	 *
	 * @param skipDeadCodeBlocks
	 *        {@code true} to skip dead code.
	 *        {@code false} to visit frames of dead code.
	 */
	public void setSkipDeadCodeBlocks(boolean skipDeadCodeBlocks) {
		this.skipDeadCodeBlocks = skipDeadCodeBlocks;
	}

	/**
	 * Called when an opaque predicate has been hit.
	 *
	 * @param insn
	 * 		The instruction that acts as an opaque predicate <i>({@link JumpInsnNode}</i>
	 * @param gotoDestination
	 *        {@code true} If the pre-determined jump goes to the destination.
	 *        {@code false} for fall-through behavior.
	 */
	public void setOpaqueJump(AbstractInsnNode insn, boolean gotoDestination) {
		if (skipDeadCodeBlocks) {
			opaqueHandler.setOpaqueJump(insn, gotoDestination);
		}
	}

	/**
	 * @return Opaque predicate handler.
	 */
	public OpaqueHandler getOpaqueHandler() {
		return opaqueHandler;
	}

	/**
	 * @return Block manager.
	 */
	public BlockHandler getBlockHandler() {
		return interpreter.getBlockHandler();
	}

	@SuppressWarnings("SuspiciousSystemArraycopy") // sus
	private static SimFrame[] copy(Frame<AbstractValue>[] values) {
		// Hiding this here because casting array wrapper type doesn't work
		// We want to make it clear to users the frame type is SimFrame.
		// This makes it so that usage doesn't force them to cast everywhere.
		SimFrame[] copy = new SimFrame[values.length];
		System.arraycopy(values, 0, copy, 0, values.length);
		return copy;
	}
}
