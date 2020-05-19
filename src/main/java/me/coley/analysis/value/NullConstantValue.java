package me.coley.analysis.value;

import me.coley.analysis.util.TypeUtil;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.List;

import static me.coley.analysis.util.CollectUtils.combine;

/**
 * Value wrapper for null constants.
 *
 * @author Matt
 */
public class NullConstantValue extends AbstractValue {
	public static Type NULL_VALUE_TYPE = TypeUtil.OBJECT_TYPE;

	protected NullConstantValue(AbstractInsnNode insn) {
		super(insn, NULL_VALUE_TYPE, null);
	}

	protected NullConstantValue(List<AbstractInsnNode> insns) {
		super(insns, NULL_VALUE_TYPE, null);
	}

	/**
	 * @param insn
	 * 		Instruction of the value.
	 *
	 * @return Null constant value.
	 */
	public static NullConstantValue newNull(AbstractInsnNode insn) { return new NullConstantValue(insn); }

	@Override
	public AbstractValue copy(AbstractInsnNode insn) {
		return new NullConstantValue(combine(getInsns(), insn));
	}

	@Override
	public boolean canMerge(AbstractValue other) {
		return other == this;
	}

	@Override
	public boolean isPrimitive() {
		return false;
	}

	@Override
	public boolean isReference() {
		return true;
	}

	@Override
	public boolean isValueResolved() {
		return true;
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof NullConstantValue;
	}
}
