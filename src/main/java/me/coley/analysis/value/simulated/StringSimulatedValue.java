package me.coley.analysis.value.simulated;

import me.coley.analysis.TypeChecker;
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
	protected StringSimulatedValue(List<AbstractInsnNode> insns, Type type, String value, TypeChecker typeChecker) {
		super(insns, type, value, typeChecker);
	}

	protected StringSimulatedValue(List<AbstractInsnNode> insns, Type type, String value,
								   GetSet<String> resultValue, TypeChecker typeChecker) {
		super(insns, type, value, resultValue, typeChecker);
	}

	/**
	 * @param insn
	 * 		Instruction of value.
	 * @param typeChecker
	 * 		Type checker for comparison against other types.
	 * @param value
	 * 		String.
	 *
	 * @return String value.
	 */
	public static StringSimulatedValue of(AbstractInsnNode insn, TypeChecker typeChecker, String value) {
		return of(Collections.singletonList(insn), typeChecker, value);
	}

	/**
	 * @param insns
	 * 		Instructions of value.
	 * @param typeChecker
	 * 		Type checker for comparison against other types.
	 * @param value
	 * 		String.
	 *
	 * @return String value.
	 */
	public static StringSimulatedValue of(List<AbstractInsnNode> insns, TypeChecker typeChecker, String value) {
		return new StringSimulatedValue(insns, Type.getObjectType("java/lang/String"), value, typeChecker);
	}

	@Override
	protected AbstractValue create(List<AbstractInsnNode> collection) {
		return new StringSimulatedValue(collection, getType(), (String) getValue(), resultValue, typeChecker);
	}

	@Override
	public AbstractValue ofVirtualInvoke(MethodInsnNode min, List<? extends AbstractValue> arguments)
			throws SimFailedException {
		// TODO: Special case handling
		return defaultOfVirtualInvoke(min, arguments);
	}
}
