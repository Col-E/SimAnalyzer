package me.coley.analysis.value;

import me.coley.analysis.Unresolved;
import org.objectweb.asm.Type;

import java.util.function.BiPredicate;

/**
 * Value wrapper recording the type. Wrapped value is a placeholder and used to denote an unresolved value.
 *
 * @author Matt
 */
public class VirtualValue extends AbstractValue {
	private static BiPredicate<Type, Type> isParent = (a, b) -> false;

	protected VirtualValue(Type type, Object value) {
		super(type, value);
	}

	/**
	 * @param type Type to virtualize.
	 * @return Virtual value of type.
	 */
	public static VirtualValue ofVirtual(Type type) {
		return new VirtualValue(type, new Unresolved(type));
	}

	/**
	 * @param value The value / type of class.
	 * @return Class value.
	 */
	public static VirtualValue ofClass(Type value) {
		return new VirtualValue(Type.getObjectType("java/lang/Class"), value);
	}

	/**
	 * @param desc Method type descriptor.
	 * @return Act on the current reference.
	 */
	public AbstractValue ofMethodRef(Type desc) {
		// Validate desc
		if (desc == null)
			throw new IllegalStateException("Method descriptor must not be null");
		// Don't act on 'null' values
		if (value == null || value.equals(Type.VOID_TYPE))
			throw new IllegalStateException("Cannot act on null reference value");
		// Don't try to do object stuff with non-objects
		if (!isReference())
			throw new IllegalStateException("Cannot act on reference on non-reference value");
		// Handle return types
		Type retType = desc.getReturnType();
		if (retType.equals(Type.VOID_TYPE))
			return null;
		if (retType.getSort() <= Type.DOUBLE)
			return new PrimitiveValue(retType);
		return ofVirtual(retType);
	}

	@Override
	public boolean canMerge(AbstractValue other) {
		if(other == this)
			return true;
		else if(other == NullConstantValue.NULL_VALUE ||
				other == UninitializedValue.UNINITIALIZED_VALUE ||
				other == null)
			return false;
		else if(type == null)
			return other.type == null;
		return type.equals(other.type) || isParent(type, other.type);
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
		return false;
	}

	@Override
	public boolean equals(Object other) {
		if (other == this)
			return true;
		else if (other == UninitializedValue.UNINITIALIZED_VALUE)
			return false;
		else if (other == NullConstantValue.NULL_VALUE)
			return false;
		else if(other instanceof VirtualValue) {
			VirtualValue rOther = (VirtualValue) other;
			if (value instanceof StringBuilder || value instanceof StringBuffer)
				return value.toString().equals(rOther.value.toString());
			else
				return type.equals(rOther.type) && value.equals(rOther.value);
		}
		return false;
	}

	protected boolean isParent(Type parent, Type child) {
		if(parent == null || child == null)
			throw new IllegalStateException("Cannot find common type of parent null type");
		else if (parent.equals(child))
			return true;
		else if(parent.getSort() == Type.OBJECT && child.getSort() == Type.OBJECT) {
			if(parent.equals(child))
				return true;
			return isParent.test(parent, child);
		} else
			return parent.getSort() < Type.ARRAY && child.getSort() < Type.ARRAY;
	}

	/**
	 * Set an additional parent check condition.
	 *
	 * @param isParent
	 * 		Predicate that takes a supposed parent and child type, giving {@code true} when the
	 * 		child parent relationship is correct.
	 */
	public static void setParentCheck(BiPredicate<Type, Type> isParent) {
		VirtualValue.isParent = isParent;
	}
}
