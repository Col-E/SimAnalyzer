package me.coley.analysis;

import me.coley.analysis.exception.LoggedAnalyzerException;
import me.coley.analysis.value.AbstractValue;
import org.objectweb.asm.tree.AbstractInsnNode;
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
	private final SimInterpreter interpreter;

	/**
	 * Create analyzer.
	 *
	 * @param interpreter
	 * 		Interpreter to use.
	 */
	public SimAnalyzer(SimInterpreter interpreter) {
		super(interpreter);
		this.interpreter = interpreter;
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
				if (e.getValue() instanceof LoggedAnalyzerException) {
					if (((LoggedAnalyzerException) e.getValue()).validate(method, values)) {
						interpreter.getProblemInsns().remove(e.getKey());
					}
				}
			}
			// Check one last time
			if (!interpreter.getProblemInsns().isEmpty())
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
}
