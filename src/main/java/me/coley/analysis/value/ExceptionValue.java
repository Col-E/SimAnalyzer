package me.coley.analysis.value;

import me.coley.analysis.TypeResolver;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Value wrapper recording exceptions.
 *
 * @author Matt Coley
 */
public class ExceptionValue extends VirtualValue {
	protected ExceptionValue(AbstractInsnNode insn, Type type, Object value, TypeResolver typeResolver) {
		super(insn, type, value, typeResolver);
	}

	/**
	 * @param handler The label where this exception spawns from.
	 * @param typeResolver Type resolver for comparison against other types.
	 * @param exceptionType Type to virtualize.
	 * @return Virtual exception value of type.
	 */
	public static AbstractValue ofHandledException(AbstractInsnNode handler, TypeResolver typeResolver, Type exceptionType) {
		return new ExceptionValue(handler, exceptionType, exceptionType, typeResolver);
	}
}
