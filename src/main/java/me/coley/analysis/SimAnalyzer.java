package me.coley.analysis;

import me.coley.analysis.exception.ResolvableAnalyzerException;
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
		opaqueHandler.setOpaqueJump(insn, gotoDestination);
	}

	/**
	 * @return Opaque predicate handler.
	 */
	public OpaqueHandler getOpaqueHandler() {
		return opaqueHandler;
	}
}
