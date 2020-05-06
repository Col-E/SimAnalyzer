package me.coley.analysis.exception;

import me.coley.analysis.SimInterpreter;
import me.coley.analysis.value.AbstractValue;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Value;

/**
 * An analyzer exception that is logged in the {@link SimInterpreter} but not immediately thrown.
 * Instead a {@link #postValidator} is run after analysis is done that checks if the cause of the
 * analysis error has been resolved.
 *
 * @author Matt
 */
public class LoggedAnalyzerException extends AnalyzerException {
	private final PostValidator postValidator;

	/**
	 * @param postValidator
	 * 		Error resolve checker.
	 * @param insn
	 * 		Instruction that caused the exception.
	 * @param message
	 * 		Additional information.
	 */
	public LoggedAnalyzerException(PostValidator postValidator, AbstractInsnNode insn,
								   String message) {
		super(insn, message);
		this.postValidator = postValidator;
	}

	/**
	 * @param postValidator
	 * 		Error resolve checker.
	 * @param insn
	 * 		Instruction that caused the exception.
	 * @param message
	 * 		Additional information.
	 * @param expected
	 * 		Expected value at instruction.
	 * @param actual
	 * 		Actual value at instruction.
	 */
	public LoggedAnalyzerException(PostValidator postValidator, AbstractInsnNode insn,
								   String message, Object expected, Value actual) {
		super(insn, message, expected, actual);
		this.postValidator = postValidator;
	}

	/**
	 * Run the validator to check if the problem is no longer applicable given the knowledge of
	 * the generated frames.
	 *
	 * @param method
	 * 		Method analyzed.
	 * @param frames
	 * 		Frames generated from analysis
	 *
	 * @return {@code true} when the problem has been resolved.
	 */
	public boolean validate(MethodNode method, Frame<AbstractValue>[] frames) {
		return postValidator.test(method, frames);
	}
}
