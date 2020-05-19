package me.coley.analysis;

import me.coley.analysis.exception.ResolvableAnalyzerException;
import me.coley.analysis.exception.ResolvableExceptionFactory;
import me.coley.analysis.value.AbstractValue;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;

import java.util.HashSet;
import java.util.Map;

/**
 * Analyzer that uses {@link SimFrame} and is based on {@link AbstractValue}s.
 *
 * @author Matt
 */
public class SimAnalyzer extends Analyzer<AbstractValue> {
	private final OpaqueHandler opaqueHandler = new OpaqueHandler(this);
	private final SimInterpreter interpreter;
	private boolean throwUnresolvedAnalyzerErrors = true;
	private boolean skipDeadCodeBlocks = true;

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
		this.interpreter.setTypeChecker(createTypeChecker());
	}

	@Override
	public Frame<AbstractValue>[] analyze(String owner, MethodNode method) throws AnalyzerException {
		Frame<AbstractValue>[] values = super.analyze(owner, method);
		// If the interpeter has problems, check if they've been resolved by checking frames
		if (interpreter.hasReportedProblems()) {
			// Check if the error logged no longer applies given the stack analysis results
			// (due to flow control most likely)
			for(Map.Entry<AbstractInsnNode, AnalyzerException> e :
					new HashSet<>(interpreter.getProblemInsns().entrySet())) {
				if (e.getValue() instanceof ResolvableAnalyzerException) {
					if (((ResolvableAnalyzerException) e.getValue()).validate(method, values)) {
						interpreter.getProblemInsns().remove(e.getKey());
					}
				}
			}
			// Check one last time
			if (throwUnresolvedAnalyzerErrors && !interpreter.getProblemInsns().isEmpty())
				throw interpreter.getProblemInsns().values().iterator().next();
		}
		return values;
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
	protected void newControlFlowEdge(int insnIndex, int successorIndex) {
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
		return new ResolvableExceptionFactory(createTypeChecker());
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
			} catch(Throwable t) {
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
}
