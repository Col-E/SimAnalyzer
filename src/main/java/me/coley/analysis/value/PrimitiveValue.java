package me.coley.analysis.value;

import me.coley.analysis.util.TypeUtil;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.List;

import static me.coley.analysis.util.CollectUtils.*;

/**
 * Value wrapper for primitives.
 *
 * @author Matt Coley
 */
public class PrimitiveValue extends AbstractValue {
	/**
	 * Create primitive when the value is given.
	 *
	 * @param insn
	 * 		The instruction of this value.
	 * @param type
	 * 		Type of primitive.
	 * @param value
	 * 		Value of primitive.
	 */
	protected PrimitiveValue(AbstractInsnNode insn, Type type, Object value) {
		super(insn, type, value);
	}

	/**
	 * Create primitive when the value is not given.
	 *
	 * @param insn
	 * 		The instruction of this value.
	 * @param type
	 * 		Type of primitive.
	 */
	public PrimitiveValue(AbstractInsnNode insn, Type type) {
		super(insn, type, null);
	}

	/**
	 * Create primitive when the value is not given.
	 *
	 * @param insns
	 * 		The instructions of this value.
	 * @param type
	 * 		Type of primitive.
	 * @param value
	 * 		Value of primitive.
	 */
	public PrimitiveValue(List<AbstractInsnNode> insns, Type type, Object value) {
		super(insns, type, value);
	}

	/**
	 * Create primitive when the value is not given.
	 *
	 * @param insns
	 * 		The instructions of this value.
	 * @param type
	 * 		Type of primitive.
	 */
	protected PrimitiveValue(List<AbstractInsnNode> insns, Type type) {
		super(insns, type, null);
	}

	/**
	 * @param insn
	 * 		The instruction of this value.
	 * @param value
	 * 		Int.
	 *
	 * @return int value.
	 */
	public static AbstractValue ofInt(AbstractInsnNode insn, int value) {
		return new PrimitiveValue(insn, Type.INT_TYPE, value);
	}

	/**
	 * @param insns
	 * 		The instructions of this value.
	 * @param value
	 * 		Int.
	 *
	 * @return int value.
	 */
	public static AbstractValue ofInt(List<AbstractInsnNode> insns, int value) {
		return new PrimitiveValue(insns, Type.INT_TYPE, value);
	}

	/**
	 * @param insn
	 * 		The instruction of this value.
	 * @param value
	 * 		Character.
	 *
	 * @return char value.
	 */
	public static AbstractValue ofChar(AbstractInsnNode insn, char value) {
		return new PrimitiveValue(insn, Type.INT_TYPE, (int) value);
	}

	/**
	 * @param insns
	 * 		The instructions of this value.
	 * @param value
	 * 		Character.
	 *
	 * @return char value.
	 */
	public static AbstractValue ofChar(List<AbstractInsnNode> insns, char value) {
		return new PrimitiveValue(insns, Type.INT_TYPE, (int) value);
	}

	/**
	 * @param insn
	 * 		The instruction of this value.
	 * @param value
	 * 		Byte.
	 *
	 * @return byte value.
	 */
	public static AbstractValue ofByte(AbstractInsnNode insn, byte value) {
		return new PrimitiveValue(insn, Type.INT_TYPE, value);
	}

	/**
	 * @param insns
	 * 		The instructions of this value.
	 * @param value
	 * 		Byte.
	 *
	 * @return byte value.
	 */
	public static AbstractValue ofByte(List<AbstractInsnNode> insns, byte value) {
		return new PrimitiveValue(insns, Type.INT_TYPE, value);
	}

	/**
	 * @param insn
	 * 		The instruction of this value.
	 * @param value
	 * 		Short.
	 *
	 * @return short value.
	 */
	public static AbstractValue ofShort(AbstractInsnNode insn, short value) {
		return new PrimitiveValue(insn, Type.INT_TYPE, value);
	}

	/**
	 * @param insns
	 * 		The instructions of this value.
	 * @param value
	 * 		Short.
	 *
	 * @return short value.
	 */
	public static AbstractValue ofShort(List<AbstractInsnNode> insns, short value) {
		return new PrimitiveValue(insns, Type.INT_TYPE, value);
	}

	/**
	 * @param insn
	 * 		The instruction of this value.
	 * @param value
	 * 		Boolean.
	 *
	 * @return boolean value.
	 */
	public static AbstractValue ofBool(AbstractInsnNode insn, boolean value) {
		return new PrimitiveValue(insn, Type.INT_TYPE, value ? 1 : 0);
	}

	/**
	 * @param insns
	 * 		The instructions of this value.
	 * @param value
	 * 		Boolean.
	 *
	 * @return boolean value.
	 */
	public static AbstractValue ofBool(List<AbstractInsnNode> insns, boolean value) {
		return new PrimitiveValue(insns, Type.INT_TYPE, value ? 1 : 0);
	}

	/**
	 * @param insn
	 * 		The instruction of this value.
	 * @param value
	 * 		Long.
	 *
	 * @return long value.
	 */
	public static AbstractValue ofLong(AbstractInsnNode insn, long value) {
		return new PrimitiveValue(insn, Type.LONG_TYPE, value);
	}

	/**
	 * @param insns
	 * 		The instructions of this value.
	 * @param value
	 * 		Long.
	 *
	 * @return long value.
	 */
	public static AbstractValue ofLong(List<AbstractInsnNode> insns, long value) {
		return new PrimitiveValue(insns, Type.LONG_TYPE, value);
	}

	/**
	 * @param insn
	 * 		The instruction of this value.
	 * @param value
	 * 		Float.
	 *
	 * @return float value.
	 */
	public static AbstractValue ofFloat(AbstractInsnNode insn, float value) {
		return new PrimitiveValue(insn, Type.FLOAT_TYPE, value);
	}

	/**
	 * @param insns
	 * 		The instructions of this value.
	 * @param value
	 * 		Float.
	 *
	 * @return float value.
	 */
	public static AbstractValue ofFloat(List<AbstractInsnNode> insns, float value) {
		return new PrimitiveValue(insns, Type.FLOAT_TYPE, value);
	}

	/**
	 * @param insn
	 * 		The instruction of this value.
	 * @param value
	 * 		Double.
	 *
	 * @return double value.
	 */
	public static AbstractValue ofDouble(AbstractInsnNode insn, double value) {
		return new PrimitiveValue(insn, Type.DOUBLE_TYPE, value);
	}

	/**
	 * @param insns
	 * 		The instruction of this value.
	 * @param value
	 * 		Double.
	 *
	 * @return double value.
	 */
	public static AbstractValue ofDouble(List<AbstractInsnNode> insns, double value) {
		return new PrimitiveValue(insns, Type.DOUBLE_TYPE, value);
	}

	/**
	 * @return Current value as boolean.
	 */
	public boolean getBooleanValue() {
		return ((Number) getValue()).intValue() > 1;
	}

	/**
	 * @return Current value as integer.
	 */
	public int getIntValue() {
		return ((Number) getValue()).intValue();
	}

	/**
	 * @return Current value as integer.
	 */
	public float getFloatValue() {
		return ((Number) getValue()).floatValue();
	}

	/**
	 * @return Current value as double.
	 */
	public double getDoubleValue() {
		return ((Number) getValue()).doubleValue();
	}

	/**
	 * @return Current value as long.
	 */
	public long getLongValue() {
		return ((Number) getValue()).longValue();
	}

	/**
	 * @param opInsn
	 * 		Instruction of the mathematical operation
	 * @param other
	 * 		Another value.
	 *
	 * @return Adds this value to another.
	 */
	public PrimitiveValue add(AbstractInsnNode opInsn, AbstractValue other) {
		Type common = commonMathType(type, other.type);
		List<AbstractInsnNode> mergedInsns = combineAdd(insns, other.insns, opInsn);
		if (isValueUnresolved() || other.isValueUnresolved())
			return new PrimitiveValue(mergedInsns, common);
		return new PrimitiveValue(mergedInsns, common, addN((Number) value, (Number) other.value));
	}

	/**
	 * @param opInsn
	 * 		Instruction of the mathematical operation
	 * @param other
	 * 		Another value.
	 *
	 * @return Subtract this value by another.
	 */
	public PrimitiveValue sub(AbstractInsnNode opInsn, AbstractValue other) {
		Type common = commonMathType(type, other.type);
		List<AbstractInsnNode> mergedInsns = combineAdd(insns, other.insns, opInsn);
		if (isValueUnresolved() || other.isValueUnresolved())
			return new PrimitiveValue(mergedInsns, common);
		return new PrimitiveValue(mergedInsns, common, subN((Number) value, (Number) other.value));
	}

	/**
	 * @param opInsn
	 * 		Instruction of the mathematical operation
	 * @param other
	 * 		Another value.
	 *
	 * @return Multiply this value by another.
	 */
	public PrimitiveValue mul(AbstractInsnNode opInsn, AbstractValue other) {
		Type common = commonMathType(type, other.type);
		List<AbstractInsnNode> mergedInsns = combineAdd(insns, other.insns, opInsn);
		if (isValueUnresolved() || other.isValueUnresolved())
			return new PrimitiveValue(mergedInsns, common);
		return new PrimitiveValue(mergedInsns, common, mulN((Number) value, (Number) other.value));
	}

	/**
	 * @param opInsn
	 * 		Instruction of the mathematical operation
	 * @param other
	 * 		Another value.
	 *
	 * @return Divide this value by another.
	 */
	public PrimitiveValue div(AbstractInsnNode opInsn, AbstractValue other) {
		Type common = commonMathType(type, other.type);
		List<AbstractInsnNode> mergedInsns = combineAdd(insns, other.insns, opInsn);
		if (isValueUnresolved() || other.isValueUnresolved())
			return new PrimitiveValue(mergedInsns, common);
		try {
			return new PrimitiveValue(mergedInsns, common, divN((Number) value, (Number) other.value));
		} catch(ArithmeticException ex) {
			return new PrimitiveValue(mergedInsns, common);
		}
	}

	/**
	 * @param opInsn
	 * 		Instruction of the mathematical operation
	 * @param other
	 * 		Another value.
	 *
	 * @return Get remainder of this value by another.
	 */
	public PrimitiveValue rem(AbstractInsnNode opInsn, AbstractValue other) {
		Type common = commonMathType(type, other.type);
		List<AbstractInsnNode> mergedInsns = combineAdd(insns, other.insns, opInsn);
		if (isValueUnresolved() || other.isValueUnresolved())
			return new PrimitiveValue(mergedInsns, common);
		try {
			return new PrimitiveValue(mergedInsns, common, remN((Number) value, (Number) other.value));
		} catch(ArithmeticException ex) {
			return new PrimitiveValue(mergedInsns, common);
		}
	}

	/**
	 * @param opInsn
	 * 		Instruction of the mathematical operation
	 * @param other
	 * 		Another value.
	 *
	 * @return Shift this value by another.
	 */
	public PrimitiveValue shl(AbstractInsnNode opInsn, AbstractValue other) {
		Type common = commonMathType(type, other.type);
		List<AbstractInsnNode> mergedInsns = combineAdd(insns, other.insns, opInsn);
		if (!(common.equals(Type.INT_TYPE) || common.equals(Type.LONG_TYPE)))
			throw new IllegalStateException("Requires int/long types");
		if (isValueUnresolved() || other.isValueUnresolved())
			return new PrimitiveValue(mergedInsns, common);
		return new PrimitiveValue(mergedInsns, common, shlN((Number) value, (Number) other.value));
	}

	/**
	 * @param opInsn
	 * 		Instruction of the mathematical operation
	 * @param other
	 * 		Another value.
	 *
	 * @return Shift this value by another.
	 */
	public PrimitiveValue shr(AbstractInsnNode opInsn, AbstractValue other) {
		Type common = commonMathType(type, other.type);
		if (!(common.equals(Type.INT_TYPE) || common.equals(Type.LONG_TYPE)))
			throw new IllegalStateException("Requires int/long types");
		List<AbstractInsnNode> mergedInsns = combineAdd(insns, other.insns, opInsn);
		if (isValueUnresolved() || other.isValueUnresolved())
			return new PrimitiveValue(mergedInsns, common);
		return new PrimitiveValue(mergedInsns, common, shrN((Number) value, (Number) other.value));
	}

	/**
	 * @param opInsn
	 * 		Instruction of the mathematical operation
	 * @param other
	 * 		Another value.
	 *
	 * @return Shift this value by another.
	 */
	public PrimitiveValue ushr(AbstractInsnNode opInsn, AbstractValue other) {
		Type common = commonMathType(type, other.type);
		if (!(common.equals(Type.INT_TYPE) || common.equals(Type.LONG_TYPE)))
			throw new IllegalStateException("Requires int/long types");
		List<AbstractInsnNode> mergedInsns = combineAdd(insns, other.insns, opInsn);
		if (isValueUnresolved() || other.isValueUnresolved())
			return new PrimitiveValue(mergedInsns, common);
		return new PrimitiveValue(mergedInsns, common, ushrN((Number) value, (Number) other.value));
	}

	/**
	 * @param opInsn
	 * 		Instruction of the mathematical operation
	 * @param other
	 * 		Another value.
	 *
	 * @return Bitwise and this and another value.
	 */
	public PrimitiveValue and(AbstractInsnNode opInsn, AbstractValue other) {
		Type common = commonMathType(type, other.type);
		if (!(common.equals(Type.INT_TYPE) || common.equals(Type.LONG_TYPE)))
			throw new IllegalStateException("Requires int/long types");
		List<AbstractInsnNode> mergedInsns = combineAdd(insns, other.insns, opInsn);
		if (isValueUnresolved() || other.isValueUnresolved())
			return new PrimitiveValue(mergedInsns, common);
		return new PrimitiveValue(mergedInsns, common, andN((Number) value, (Number) other.value));
	}

	/**
	 * @param opInsn
	 * 		Instruction of the mathematical operation
	 * @param other
	 * 		Another value.
	 *
	 * @return Bitwise or this and another value.
	 */
	public PrimitiveValue or(AbstractInsnNode opInsn, AbstractValue other) {
		Type common = commonMathType(type, other.type);
		if (!(common.equals(Type.INT_TYPE) || common.equals(Type.LONG_TYPE)))
			throw new IllegalStateException("Requires int/long types");
		List<AbstractInsnNode> mergedInsns = combineAdd(insns, other.insns, opInsn);
		if (value == null || other.value == null)
			return new PrimitiveValue(mergedInsns, common);
		if (value instanceof Unresolved || other.value instanceof Unresolved)
			return new PrimitiveValue(mergedInsns, common);
		return new PrimitiveValue(mergedInsns, common, orN((Number) value, (Number) other.value));
	}

	/**
	 * @param opInsn
	 * 		Instruction of the mathematical operation
	 * @param other
	 * 		Another value.
	 *
	 * @return Bitwise or this and another value.
	 */
	public PrimitiveValue xor(AbstractInsnNode opInsn, AbstractValue other) {
		Type common = commonMathType(type, other.type);
		if (!(common.equals(Type.INT_TYPE) || common.equals(Type.LONG_TYPE)))
			throw new IllegalStateException("Requires int/long types");
		List<AbstractInsnNode> mergedInsns = combineAdd(insns, other.insns, opInsn);
		if (isValueUnresolved() || other.isValueUnresolved())
			return new PrimitiveValue(mergedInsns, common);
		return new PrimitiveValue(mergedInsns, common, xorN((Number) value, (Number) other.value));
	}

	@Override
	protected PrimitiveValue create(List<AbstractInsnNode> collection) {
		return new PrimitiveValue(collection, getType(), getValue());
	}

	@Override
	public boolean canMerge(AbstractValue other) {
		if (other == this)
			return true;
		else if (!other.isPrimitive())
			return false;
		return type.equals(other.type) || ((PrimitiveValue) other).isPromotionOf(this);
	}

	@Override
	public boolean isReference() {
		return false;
	}

	@Override
	public boolean isValueResolved() {
		return (value != null) && !(value instanceof Unresolved);
	}

	@Override
	public boolean isArray() {
		return false;
	}

	@Override
	public boolean isPrimitive() {
		return true;
	}

	@Override
	public boolean equals(Object other) {
		if (other == this)
			return true;
		else if (other instanceof PrimitiveValue) {
			PrimitiveValue pOther = (PrimitiveValue) other;
			if (isValueUnresolved() || pOther.isValueUnresolved())
				return type.equals(pOther.type);
			return type.equals(pOther.type) && value.equals(pOther.value);
		}
		// Other is not a primitive
		return false;
	}

	private boolean isPromotionOf(AbstractValue other) {
		int i1 = TypeUtil.getPromotionIndex(type.getSort());
		int i2 = TypeUtil.getPromotionIndex(other.getType().getSort());
		return i1 >= i2;
	}

	private static Number addN(Number a, Number b) {
		if(a instanceof Double || b instanceof Double)
			return a.doubleValue() + b.doubleValue();
		else if(a instanceof Float || b instanceof Float)
			return a.floatValue() + b.floatValue();
		else if(a instanceof Long || b instanceof Long)
			return a.longValue() + b.longValue();
		else
			return a.intValue() + b.intValue();
	}

	private static Number subN(Number a, Number b) {
		if(a instanceof Double || b instanceof Double)
			return a.doubleValue() - b.doubleValue();
		else if(a instanceof Float || b instanceof Float)
			return a.floatValue() - b.floatValue();
		else if(a instanceof Long || b instanceof Long)
			return a.longValue() - b.longValue();
		else
			return a.intValue() - b.intValue();
	}

	private static Number mulN(Number a, Number b) {
		if(a instanceof Double || b instanceof Double)
			return a.doubleValue() * b.doubleValue();
		else if(a instanceof Float || b instanceof Float)
			return a.floatValue() * b.floatValue();
		else if(a instanceof Long || b instanceof Long)
			return a.longValue() * b.longValue();
		else
			return a.intValue() * b.intValue();
	}

	private static Number divN(Number a, Number b) {
		if(a instanceof Double || b instanceof Double)
			return a.doubleValue() / b.doubleValue();
		else if(a instanceof Float || b instanceof Float)
			return a.floatValue() / b.floatValue();
		else if(a instanceof Long || b instanceof Long)
			return a.longValue() / b.longValue();
		else
			return a.intValue() / b.intValue();
	}

	private static Number remN(Number a, Number b) {
		if(a instanceof Double || b instanceof Double)
			return a.doubleValue() % b.doubleValue();
		else if(a instanceof Float || b instanceof Float)
			return a.floatValue() % b.floatValue();
		else if(a instanceof Long || b instanceof Long)
			return a.longValue() % b.longValue();
		else
			return a.intValue() % b.intValue();
	}

	private static Number shlN(Number a, Number b) {
		if(a instanceof Long || b instanceof Long)
			return a.longValue() << b.longValue();
		else
			return a.intValue() << b.intValue();
	}

	private static Number shrN(Number a, Number b) {
		if(a instanceof Long || b instanceof Long)
			return a.longValue() >> b.longValue();
		else
			return a.intValue() >> b.intValue();
	}

	private static Number ushrN(Number a, Number b) {
		if(a instanceof Long || b instanceof Long)
			return a.longValue() >>> b.longValue();
		else
			return a.intValue() >>> b.intValue();
	}

	private static Number andN(Number a, Number b) {
		if(a instanceof Long || b instanceof Long)
			return a.longValue() & b.longValue();
		else
			return a.intValue() & b.intValue();
	}

	private static Number orN(Number a, Number b) {
		if(a instanceof Long || b instanceof Long)
			return a.longValue() | b.longValue();
		else
			return a.intValue() | b.intValue();
	}

	private static Number xorN(Number a, Number b) {
		if(a instanceof Long || b instanceof Long)
			return a.longValue() ^ b.longValue();
		else
			return a.intValue() ^ b.intValue();
	}

	private static Type commonMathType(Type a, Type b) {
		if (a == null || b == null)
			throw new IllegalStateException("Cannot find common type of a null type");
		int i1 = TypeUtil.getPromotionIndex(a.getSort());
		int i2 = TypeUtil.getPromotionIndex(b.getSort());
		int max = Math.max(i1, i2);
		if(max <= Type.DOUBLE)
			return max == i1 ? a : b;
		throw new IllegalStateException("Cannot do math on non-primitive types: " +
				a.getDescriptor() + " & " + b.getDescriptor());
	}
}
