package me.coley.analysis.value;

import me.coley.analysis.TypeChecker;
import me.coley.analysis.util.TypeUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.analysis.Value;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Value wrapper recording the type and value.
 *
 * @author Matt
 */
public abstract class AbstractValue implements Value {
	protected final Type type;
	protected final Object value;
	protected final List<AbstractInsnNode> insns;
	private JumpInsnNode nullCheck;
	private AbstractValue copySource;

	protected AbstractValue(AbstractInsnNode insn, Type type, final Object value) {
		this(insn == null ? Collections.emptyList() : Collections.singletonList(insn), type, value);
	}

	protected AbstractValue(List<AbstractInsnNode> insns, Type type, final Object value) {
		// Set contributing insns
		if (insns == null)
			insns = Collections.emptyList();
		this.insns = insns;
		// Validate type must exist if value given
		if (type == null && value != null)
			throw new IllegalStateException("Analyzer value wrapper was given a value but no type information");
		// Upgrade types to int
		if (type != null && type.getSort() >= Type.BOOLEAN && type.getSort() < Type.INT)
			type = Type.INT_TYPE;
		this.type = type;
		this.value = value;
	}

	/**
	 * @param insn The instruction of the value.
	 * @param typeChecker Type checker for comparison against other types.
	 * @param type Type.
	 * @return Type value.
	 */
	public static AbstractValue ofDefault(AbstractInsnNode insn, TypeChecker typeChecker, Type type) {
		if (type == null)
			return UninitializedValue.UNINITIALIZED_VALUE;
		switch(type.getSort()) {
			case Type.VOID:
				return null;
			case Type.BOOLEAN:
			case Type.CHAR:
			case Type.BYTE:
			case Type.SHORT:
			case Type.INT:
				return PrimitiveValue.ofInt(insn, 0);
			case Type.FLOAT:
				return PrimitiveValue.ofFloat(insn, 0F);
			case Type.LONG:
				return PrimitiveValue.ofLong(insn, 0L);
			case Type.DOUBLE:
				return PrimitiveValue.ofDouble(insn, 0D);
			case Type.ARRAY:
			case Type.OBJECT:
				if (type.equals(NullConstantValue.NULL_VALUE_TYPE))
					return NullConstantValue.newNull(insn);
				return new VirtualValue(insn, type, null, typeChecker);
			default:
				throw new IllegalStateException("Unsupported type: " + type);
		}
	}

	/**
	 * @param insn
	 * 		Instruction to add to copy instance.
	 *
	 * @return Copy of current value, with additional instruction added.
	 */
	public abstract AbstractValue copy(AbstractInsnNode insn);

	/**
	 * Copies any additional values from the current value to the given copy.
	 *
	 * @param copy
	 * 		Copied value.
	 *
	 * @return Copied value.
	 */
	protected AbstractValue onCopy(AbstractValue copy) {
		copy.setNullCheckedBy(getNullCheck());
		copy.copySource = this;
		return copy;
	}

	/**
	 * @param other
	 * 		Another frame.
	 *
	 * @return {@code true} if other can be merged into this.
	 */
	public abstract boolean canMerge(AbstractValue other);

	/**
	 * @return {@code true} if the value represents a primitive.
	 */
	public abstract boolean isPrimitive();

	/**
	 * @return {@code true} if the wrapped type is a reference.
	 */
	public abstract boolean isReference();

	/**
	 * @return {@code true} when the exact value is known.
	 */
	public abstract boolean isValueResolved();

	/**
	 * @return {@code true} when the exact value is not known.
	 */
	public boolean isValueUnresolved() {
		return !isValueResolved();
	}

	/**
	 * @return {@code true} if the {@link #getValue() value} is {@code null}.
	 */
	public boolean isNull() {
		return value == null;
	}

	/**
	 * @return {@code true} if this RValue contains an array type.
	 */
	public boolean isArray() {
		return type != null && type.getSort() == Type.ARRAY;
	}

	/**
	 * @return Value.
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @return Type.
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Returns the instructions that contributed to the current value.
	 * These instructions may not necessarily be in order due to merge behavior of two or more values.
	 * Additionally, some values are not defined by instructions, such as parameter values.
	 *
	 * @return Instructions that contributed to the current value.
	 */
	public List<AbstractInsnNode> getInsns() {
		return insns;
	}

	/**
	 * @param nullCheck
	 * 		Instruction that checks the current value against {@code null}.
	 * 		Either {@link Opcodes#IFNULL} or {@link Opcodes#IFNONNULL}.
	 */
	public void setNullCheckedBy(JumpInsnNode nullCheck) {
		this.nullCheck = nullCheck;
		if (copySource != null)
			copySource.setNullCheckedBy(nullCheck);
	}

	/**
	 * @return Instruction that checks the current value against {@code null}.
	 * Either {@link Opcodes#IFNULL} or {@link Opcodes#IFNONNULL}.
	 */
	public JumpInsnNode getNullCheck() {
		return nullCheck;
	}

	@Override
	public int getSize() {
		if (type == null)
			return 1;
		return TypeUtil.sortToSize(type.getSort());
	}

	@Override
	public int hashCode() {
		if (type == null)
			return 0;
		if (value == null)
			return type.hashCode();
		return Objects.hash(type.getDescriptor(), value);
	}

	@Override
	public abstract boolean equals(Object other);

	@Override
	public String toString() {
		if (this == UninitializedValue.UNINITIALIZED_VALUE)
			return "<UNINITIALIZED>";
		else if (this instanceof NullConstantValue)
			return "<NULL>";
		else if (this instanceof ReturnAddressValue)
			return "<JSR_RET>";
		else if (isNull())
			return "<NULL:" + type + ">";
		else
			return "<" + type + ":" + value + ">";
	}
}
