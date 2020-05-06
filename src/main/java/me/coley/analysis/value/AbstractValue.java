package me.coley.analysis.value;

import me.coley.analysis.util.TypeUtil;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.analysis.Value;

import java.util.Objects;

/**
 * Value wrapper recording the type and value.
 *
 * @author Matt
 */
public abstract class AbstractValue implements Value {
	protected final Type type;
	protected final Object value;

	protected AbstractValue(Type type, final Object value) {
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
	 * @param type Type.
	 * @return Type value.
	 */
	public static AbstractValue ofDefault(Type type) {
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
				return PrimitiveValue.ofInt(0);
			case Type.FLOAT:
				return PrimitiveValue.ofFloat(0F);
			case Type.LONG:
				return PrimitiveValue.ofLong(0L);
			case Type.DOUBLE:
				return PrimitiveValue.ofDouble(0D);
			case Type.ARRAY:
			case Type.OBJECT:
				if (type.equals(NullConstantValue.NULL_VALUE.getType()))
					return NullConstantValue.NULL_VALUE;
				return new VirtualValue(type, null);
			default:
				throw new IllegalStateException("Unsupported type: " + type);
		}
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

	/*{
		if (other == this)
			return true;
		else if (other == UNINITIALIZED)
			return false;
		else if (other == NULL)
			return false;
		else if(other instanceof AbstractValue) {
			AbstractValue ov = (AbstractValue) other;
			if(type == null)
				// RET: Are both types null?
				return ov.type == null;
			else if(value == null)
				// RET: Do they share a parent? And is the other value also null?
				return (type.equals(ov.type) || isParent(type, ov.type)) && ov.value == null;
			else
				// RET: Are the values equal? Do they share a parent?
				return value.equals(ov.value) && (type.equals(ov.type) || isParent(type, ov.type));
		}
		return false;
	}*/

	@Override
	public String toString() {
		if (this == UninitializedValue.UNINITIALIZED_VALUE)
			return "<UNINITIALIZED>";
		else if (this == NullConstantValue.NULL_VALUE)
			return "<NULL>";
		else if (this == ReturnAddressValue.RETURN_ADDRESS_VALUE)
			return "<JSR_RET>";
		else if (isNull())
			return "<NULL:" + type + ">";
		else
			return "<" + type + ":" + value + ">";
	}
}
