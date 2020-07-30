package me.coley.analysis.value.simulated;

import me.coley.analysis.TypeChecker;
import me.coley.analysis.exception.SimFailedException;
import me.coley.analysis.util.CollectUtils;
import me.coley.analysis.util.GetSet;
import me.coley.analysis.value.AbstractValue;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.Collections;
import java.util.List;

import static me.coley.analysis.util.CollectUtils.*;

/**
 * Value recording the current string value.
 *
 * @author Matt
 */
public class StringValue extends AbstractSimulatedValue<String> {
	protected StringValue(List<AbstractInsnNode> insns, Type type, String value, TypeChecker typeChecker) {
		super(insns, type, value, typeChecker);
	}

	protected StringValue(List<AbstractInsnNode> insns, Type type, String value, GetSet<String> resultValue, TypeChecker typeChecker) {
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
	public static StringValue of(AbstractInsnNode insn, TypeChecker typeChecker, String value) {
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
	public static StringValue of(List<AbstractInsnNode> insns, TypeChecker typeChecker, String value) {
		return new StringValue(insns, Type.getObjectType("java/lang/String"), value, typeChecker);
	}

	@Override
	public AbstractValue copy(AbstractInsnNode insn) {
		return new StringValue(add(getInsns(), insn), getType(), (String) getValue(), resultValue, typeChecker);
	}

	@Override
	public AbstractValue ofVirtualInvoke(MethodInsnNode min, List<? extends AbstractValue> arguments) throws SimFailedException {
		// TODO: Special case handling
		return defaultOfVirtualInvoke(min, arguments);
	}
}
