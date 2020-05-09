package me.coley.analysis.value;

import me.coley.analysis.Unresolved;
import me.coley.analysis.util.TypeUtil;
import org.objectweb.asm.Type;

/**
 * Value wrapper for primitives.
 *
 * @author Matt
 */
public class PrimitiveValue extends AbstractValue {
	/**
	 * Create primitive when the value is given.
	 *
	 * @param type
	 * 		Type of primitive.
	 * @param value
	 * 		Value of primitive.
	 */
	protected PrimitiveValue(Type type, Object value) {
		super(type, value);
	}

	/**
	 * Create primitive when the value is not given.
	 *
	 * @param type
	 * 		Type of primitive.
	 */
	public PrimitiveValue(Type type) {
		super(type, null);
	}

	/**
	 * @param value
	 * 		Int.
	 *
	 * @return int value.
	 */
	public static AbstractValue ofInt(int value) {
		return new PrimitiveValue(Type.INT_TYPE, value);
	}

	/**
	 * @param value
	 * 		Character.
	 *
	 * @return char value.
	 */
	public static AbstractValue ofChar(char value) {
		return new PrimitiveValue(Type.INT_TYPE, value);
	}

	/**
	 * @param value
	 * 		Byte.
	 *
	 * @return byte value.
	 */
	public static AbstractValue ofByte(byte value) {
		return new PrimitiveValue(Type.INT_TYPE, value);
	}

	/**
	 * @param value
	 * 		Short.
	 *
	 * @return short value.
	 */
	public static AbstractValue ofShort(short value) {
		return new PrimitiveValue(Type.INT_TYPE, value);
	}

	/**
	 * @param value
	 * 		Boolean.
	 *
	 * @return boolean value.
	 */
	public static AbstractValue ofBool(boolean value) {
		return new PrimitiveValue(Type.INT_TYPE, value ? 1 : 0);
	}

	/**
	 * @param value
	 * 		Long.
	 *
	 * @return long value.
	 */
	public static AbstractValue ofLong(long value) {
		return new PrimitiveValue(Type.LONG_TYPE, value);
	}

	/**
	 * @param value
	 * 		Float.
	 *
	 * @return float value.
	 */
	public static AbstractValue ofFloat(float value) {
		return new PrimitiveValue(Type.FLOAT_TYPE, value);
	}

	/**
	 * @param value
	 * 		Double.
	 *
	 * @return double value.
	 */
	public static AbstractValue ofDouble(double value) {
		return new PrimitiveValue(Type.DOUBLE_TYPE, value);
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
	 * @param other
	 * 		Another value.
	 *
	 * @return Adds this value to another.
	 */
	public PrimitiveValue add(AbstractValue other) {
		Type common = commonMathType(type, other.type);
		if (isValueUnresolved() || other.isValueUnresolved())
			return new PrimitiveValue(common);
		return new PrimitiveValue(common, addN((Number) value, (Number) other.value));
	}

	/**
	 * @param other
	 * 		Another value.
	 *
	 * @return Subtract this value by another.
	 */
	public PrimitiveValue sub(AbstractValue other) {
		Type common = commonMathType(type, other.type);
		if (isValueUnresolved() || other.isValueUnresolved())
			return new PrimitiveValue(common);
		return new PrimitiveValue(common, subN((Number) value, (Number) other.value));
	}

	/**
	 * @param other
	 * 		Another value.
	 *
	 * @return Multiply this value by another.
	 */
	public PrimitiveValue mul(AbstractValue other) {
		Type common = commonMathType(type, other.type);
		if (isValueUnresolved() || other.isValueUnresolved())
			return new PrimitiveValue(common);
		return new PrimitiveValue(common, mulN((Number) value, (Number) other.value));
	}

	/**
	 * @param other
	 * 		Another value.
	 *
	 * @return Divide this value by another.
	 */
	public PrimitiveValue div(AbstractValue other) {
		Type common = commonMathType(type, other.type);
		if (isValueUnresolved() || other.isValueUnresolved())
			return new PrimitiveValue(common);
		try {
			return new PrimitiveValue(common, divN((Number) value, (Number) other.value));
		} catch(ArithmeticException ex) {
			return new PrimitiveValue(common);
		}
	}

	/**
	 * @param other
	 * 		Another value.
	 *
	 * @return Get remainder of this value by another.
	 */
	public PrimitiveValue rem(AbstractValue other) {
		Type common = commonMathType(type, other.type);
		if (isValueUnresolved() || other.isValueUnresolved())
			return new PrimitiveValue(common);
		try {
			return new PrimitiveValue(common, remN((Number) value, (Number) other.value));
		} catch(ArithmeticException ex) {
			return new PrimitiveValue(common);
		}
	}

	/**
	 * @param other
	 * 		Another value.
	 *
	 * @return Shift this value by another.
	 */
	public PrimitiveValue shl(AbstractValue other) {
		Type common = commonMathType(type, other.type);
		if (!(common.equals(Type.INT_TYPE) || common.equals(Type.LONG_TYPE)))
			throw new IllegalStateException("Requires int/long types");
		if (isValueUnresolved() || other.isValueUnresolved())
			return new PrimitiveValue(common);
		return new PrimitiveValue(common, shlN((Number) value, (Number) other.value));
	}

	/**
	 * @param other
	 * 		Another value.
	 *
	 * @return Shift this value by another.
	 */
	public PrimitiveValue shr(AbstractValue other) {
		Type common = commonMathType(type, other.type);
		if (!(common.equals(Type.INT_TYPE) || common.equals(Type.LONG_TYPE)))
			throw new IllegalStateException("Requires int/long types");
		if (isValueUnresolved() || other.isValueUnresolved())
			return new PrimitiveValue(common);
		return new PrimitiveValue(common, shrN((Number) value, (Number) other.value));
	}

	/**
	 * @param other
	 * 		Another value.
	 *
	 * @return Shift this value by another.
	 */
	public PrimitiveValue ushr(AbstractValue other) {
		Type common = commonMathType(type, other.type);
		if (!(common.equals(Type.INT_TYPE) || common.equals(Type.LONG_TYPE)))
			throw new IllegalStateException("Requires int/long types");
		if (isValueUnresolved() || other.isValueUnresolved())
			return new PrimitiveValue(common);
		return new PrimitiveValue(common, ushrN((Number) value, (Number) other.value));
	}

	/**
	 * @param other
	 * 		Another value.
	 *
	 * @return Bitwise and this and another value.
	 */
	public PrimitiveValue and(AbstractValue other) {
		Type common = commonMathType(type, other.type);
		if (!(common.equals(Type.INT_TYPE) || common.equals(Type.LONG_TYPE)))
			throw new IllegalStateException("Requires int/long types");
		if (isValueUnresolved() || other.isValueUnresolved())
			return new PrimitiveValue(common);
		return new PrimitiveValue(common, andN((Number) value, (Number) other.value));
	}

	/**
	 * @param other
	 * 		Another value.
	 *
	 * @return Bitwise or this and another value.
	 */
	public PrimitiveValue or(AbstractValue other) {
		Type common = commonMathType(type, other.type);
		if (!(common.equals(Type.INT_TYPE) || common.equals(Type.LONG_TYPE)))
			throw new IllegalStateException("Requires int/long types");
		if (value == null || other.value == null)
			return new PrimitiveValue(common);
		if (value instanceof Unresolved || other.value instanceof Unresolved)
			return new PrimitiveValue(common);
		return new PrimitiveValue(common, orN((Number) value, (Number) other.value));
	}

	/**
	 * @param other
	 * 		Another value.
	 *
	 * @return Bitwise or this and another value.
	 */
	public PrimitiveValue xor(AbstractValue other) {
		Type common = commonMathType(type, other.type);
		if (!(common.equals(Type.INT_TYPE) || common.equals(Type.LONG_TYPE)))
			throw new IllegalStateException("Requires int/long types");
		if (isValueUnresolved() || other.isValueUnresolved())
			return new PrimitiveValue(common);
		return new PrimitiveValue(common, xorN((Number) value, (Number) other.value));
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
