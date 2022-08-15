package me.coley.analysis.value;

import me.coley.analysis.TypeChecker;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Value wrapper recording exceptions.
 *
 * @author Matt Coley
 */
public class ExceptionValue extends VirtualValue {
	protected ExceptionValue(AbstractInsnNode insn, Type type, Object value, TypeChecker typeChecker) {
		super(insn, type, value, typeChecker);
	}

	/**
	 * @param handler The label where this exception spawns from.
	 * @param typeChecker Type checker for comparison against other types.
	 * @param exceptionType Type to virtualize.
	 * @return Virtual exception value of type.
	 */
	public static AbstractValue ofHandledException(AbstractInsnNode handler, TypeChecker typeChecker, Type exceptionType) {
		return new ExceptionValue(handler, exceptionType, exceptionType, typeChecker);
	}
}
