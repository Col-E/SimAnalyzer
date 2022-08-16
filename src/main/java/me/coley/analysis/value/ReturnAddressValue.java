package me.coley.analysis.value;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.List;

/**
 * Value wrapper for return addresses.
 *
 * @author Matt Coley
 */
public class ReturnAddressValue extends AbstractValue {
	private ReturnAddressValue(AbstractInsnNode insn) {
		super(insn, Type.VOID_TYPE, null);
	}

	private ReturnAddressValue(List<AbstractInsnNode> insns) {
		super(insns, Type.VOID_TYPE, null);
	}

	/**
	 * @param insn
	 * 		Instruction of the value.
	 *
	 * @return Return address value.
	 */
	public static ReturnAddressValue newRet(AbstractInsnNode insn) {
		return new ReturnAddressValue(insn);
	}

	@Override
	protected ReturnAddressValue create(List<AbstractInsnNode> collection) {
		return new ReturnAddressValue(collection);
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
		return false;
	}

	@Override
	public boolean isValueResolved() {
		return true;
	}

	@Override
	public boolean equals(Object other) {
		return other == this;
	}
}
