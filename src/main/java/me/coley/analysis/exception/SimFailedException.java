package me.coley.analysis.exception;

import me.coley.analysis.value.simulated.AbstractSimulatedValue;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

/**
 * Exception for indicating a method could not be invoked by {@link AbstractSimulatedValue}.
 *
 * @author Matt Coley
 */
public class SimFailedException extends AnalyzerException {
	/**
	 * @param insn
	 * 		Instruction that could not be simulated.
	 * @param message
	 * 		Additional information.
	 * @param cause
	 * 		Root cause.
	 */
	public SimFailedException(MethodInsnNode insn, String message, Throwable cause) {
		super(insn, message, cause);
	}

	/**
	 * @param insn
	 * 		Instruction that could not be simulated.
	 * @param message
	 * 		Additional information.
	 */
	public SimFailedException(MethodInsnNode insn, String message) {
		super(insn, message);
	}
}
