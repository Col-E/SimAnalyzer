package me.coley.analysis.value.simulated;

import me.coley.analysis.TypeResolver;
import me.coley.analysis.exception.SimFailedException;
import me.coley.analysis.util.GetSet;
import me.coley.analysis.value.AbstractValue;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.Collections;
import java.util.List;

/**
 * Value recording the current string value.
 *
 * @author Matt Coley
 */
public class StringSimulatedValue extends AbstractSimulatedValue<String> {
	protected StringSimulatedValue(List<AbstractInsnNode> insns, Type type, String value, TypeResolver typeResolver) {
		super(insns, type, value, typeResolver);
	}

	protected StringSimulatedValue(List<AbstractInsnNode> insns, Type type, String value,
								   GetSet<String> resultValue, TypeResolver typeResolver) {
		super(insns, type, value, resultValue, typeResolver);
	}

	/**
	 * @param insn
	 * 		Instruction of value.
	 * @param typeResolver
	 * 		Type resolver for comparison against other types.
	 * @param value
	 * 		String.
	 *
	 * @return String value.
	 */
	public static StringSimulatedValue of(AbstractInsnNode insn, TypeResolver typeResolver, String value) {
		return of(Collections.singletonList(insn), typeResolver, value);
	}

	/**
	 * @param insns
	 * 		Instructions of value.
	 * @param typeResolver
	 * 		Type resolver for comparison against other types.
	 * @param value
	 * 		String.
	 *
	 * @return String value.
	 */
	public static StringSimulatedValue of(List<AbstractInsnNode> insns, TypeResolver typeResolver, String value) {
		return new StringSimulatedValue(insns, Type.getObjectType("java/lang/String"), value, typeResolver);
	}

	@Override
	protected AbstractValue create(List<AbstractInsnNode> collection) {
		return new StringSimulatedValue(collection, getType(), (String) getValue(), resultValue, typeResolver);
	}

	@Override
	public AbstractValue ofVirtualInvoke(MethodInsnNode min, List<? extends AbstractValue> arguments)
			throws SimFailedException {
		// TODO: Special case handling
		return defaultOfVirtualInvoke(min, arguments);
	}
}
