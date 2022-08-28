package me.coley.analysis.value;

import me.coley.analysis.TypeChecker;
import me.coley.analysis.util.CollectUtils;
import me.coley.analysis.util.TypeUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.analysis.Value;

import java.util.*;

import static me.coley.analysis.util.CollectUtils.add;

/**
 * Value wrapper recording the type and value.
 *
 * @author Matt Coley
 */
public abstract class AbstractValue implements Value {
	protected final Type type;
	protected final Object value;
	protected final List<AbstractInsnNode> insns;
	private JumpInsnNode nullCheck;
	private AbstractValue copySource;

	protected AbstractValue(AbstractInsnNode insn, Type type, final Object value) {
		this(insn == null ? new ArrayList<>() : CollectUtils.of(insn), type, value);
	}

	protected AbstractValue(List<AbstractInsnNode> insns, Type type, final Object value) {
		// Set contributing insns
		if (insns == null)
			insns = new ArrayList<>();
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

	protected abstract AbstractValue create(List<AbstractInsnNode> collection);

	/**
	 * @param insn
	 * 		The instruction of the value.
	 * @param typeChecker
	 * 		Type checker for comparison against other types.
	 * @param type
	 * 		Type.
	 *
	 * @return Type value.
	 */
	public static AbstractValue ofDefault(AbstractInsnNode insn, TypeChecker typeChecker, Type type) {
		if (type == null)
			return UninitializedValue.UNINITIALIZED_VALUE;
		switch (type.getSort()) {
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
	 * @param <A>
	 * 		Inferred self type.
	 *
	 * @return Copy of current value, with additional instruction added.
	 */
	@SuppressWarnings("unchecked")
	public final <A extends AbstractValue> A copy(AbstractInsnNode insn) {
		AbstractValue copy = create(add(getInsns(), insn));
		copy.setNullCheckedBy(getNullCheck());
		copy.copySource = this;
		return (A) copy;
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
	 * @return {@code true} if the value represents a wide primitive.
	 */
	public boolean isWide() {
		return isPrimitive() && getType().getSize() == 2;
	}

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
		return !isPrimitive() && value == null;
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
		return Collections.unmodifiableList(insns);
	}

	/**
	 * @param contributing
	 * 		Single instruction that contributes.
	 */
	public void addContributing(AbstractInsnNode contributing) {
		if (!insns.contains(contributing))
			insns.add(contributing);
	}

	/**
	 * @param contributing
	 * 		Instructions that contribute.
	 */
	public void addContributing(Collection<AbstractInsnNode> contributing) {
		contributing = new ArrayList<>(contributing);
		contributing.removeAll(insns);
		insns.addAll(contributing);
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
			return "<UNINIT>";
		else if (this instanceof NullConstantValue)
			return "<NULL>";
		else if (this instanceof ReturnAddressValue)
			return "<JSR_RET>";
		else if (isNull())
			return "<" + type + ":NULL>";
		else if (isValueResolved())
			return "<" + type + ":" + value + ">";
		else
			return "<" + type + ">";
	}
}
