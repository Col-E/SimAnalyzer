package me.coley.analysis.util;

import me.coley.analysis.TypeChecker;
import me.coley.analysis.value.AbstractValue;
import me.coley.analysis.value.NullConstantValue;
import me.coley.analysis.value.UninitializedValue;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for ASM's {@link Type} class <i>(And some additional descriptor cases)</i>
 *
 * @author Matt Coley
 */
public class TypeUtil {
	private static final List<Integer> SORT_ORDER = new ArrayList<>();
	public static final Type OBJECT_TYPE = Type.getObjectType("java/lang/Object");
	public static final Type EXCEPTION_TYPE = Type.getObjectType("java/lang/Exception");
	public static final Type BOOLEAN_ARRAY_TYPE = Type.getType("[Z");
	public static final Type CHAR_ARRAY_TYPE = Type.getType("[C");
	public static final Type BYTE_ARRAY_TYPE = Type.getType("[B");
	public static final Type SHORT_ARRAY_TYPE = Type.getType("[S");
	public static final Type INT_ARRAY_TYPE = Type.getType("[I");
	public static final Type FLOAT_ARRAY_TYPE = Type.getType("[F");
	public static final Type DOUBLE_ARRAY_TYPE = Type.getType("[D");
	public static final Type LONG_ARRAY_TYPE = Type.getType("[J");

	/**
	 * @param desc
	 *            Type to check.
	 * @return Type denotes a primitive type.
	 */
	public static boolean isPrimitiveDesc(String desc) {
		if(desc.length() != 1) {
			return false;
		}
		switch(desc.charAt(0)) {
			case 'Z':
			case 'C':
			case 'B':
			case 'S':
			case 'I':
			case 'F':
			case 'J':
			case 'D':
				return true;
			default:
				return false;
		}
	}

	/**
	 * @param a
	 * 		First type.
	 * @param b
	 * 		Second type.
	 *
	 * @return Common type shared by given types.
	 */
	public static Type commonMathType(Type a, Type b) {
		if (a == null || b == null)
			throw new IllegalStateException("Cannot find common type of a null type");
		int i1 = getPromotionIndex(a.getSort());
		int i2 = getPromotionIndex(b.getSort());
		int max = Math.max(i1, i2);
		if(max <= Type.DOUBLE)
			return max == i1 ? a : b;
		throw new IllegalStateException("Cannot do math on non-primitive types: " +
				a.getDescriptor() + " & " + b.getDescriptor());
	}

	/**
	 * @param sort
	 * 		Type sort<i>(kind)</i>
	 *
	 * @return Size of type.
	 */
	public static int sortToSize(int sort) {
		switch(sort) {
			case Type.LONG:
			case Type.DOUBLE:
				return 2;
			default:
				return 1;
		}
	}

	/**
	 * @param sort
	 * 		Method sort.
	 *
	 * @return Promotion order.
	 */
	public static int getPromotionIndex(int sort) {
		return SORT_ORDER.indexOf(sort);
	}

	/**
	 * Helper call for {@link #isSubTypeOfOrNull(TypeChecker, AbstractValue, Type)}.
	 *
	 * @param typeChecker
	 * 		Type checker for comparison against other types.
	 * @param childValue
	 * 		Some value that has a type.
	 * @param parentValue
	 * 		Some other value that has a type.
	 *
	 * @return {@code true} when the child value's type is a subtype of the parent value's type,
	 * or the child value is {@code null}.
	 */
	public static boolean isSubTypeOfOrNull(TypeChecker typeChecker, AbstractValue childValue, AbstractValue parentValue) {
		return isSubTypeOfOrNull(typeChecker, childValue, parentValue.getType());
	}

	/**
	 * @param typeChecker
	 *      Type checker for comparison against other types.
	 * @param childValue
	 * 		Some value that has a type.
	 * @param parent
	 * 		Some other type.
	 *
	 * @return {@code true} when the child value's type is a subtype of the parent,
	 * or the child value is {@code null}.
	 */
	public static boolean isSubTypeOfOrNull(TypeChecker typeChecker, AbstractValue childValue, Type parent) {
		// TODO: This should not occur
		if (childValue == null)
			return false;
		// Null type and primitives do not mix.
		// Null types and object types do.
		if (childValue instanceof NullConstantValue && !isPrimitive(parent))
			return true;
		// Uninitialized values are not subtypes
		if (childValue == UninitializedValue.UNINITIALIZED_VALUE)
			return false;
		// Fallback
		return isSubTypeOf(typeChecker, childValue.getType(), parent);
	}

	/**
	 * @param typeChecker Type checker for comparison against other types.
	 * @param child Some type.
	 * @param parent Some other type.
	 * @return {@code true} when the child is a subtype of the parent.
	 */
	public static boolean isSubTypeOf(TypeChecker typeChecker, Type child, Type parent) {
		// Can't handle null type
		if (child == null)
			return false;
		// Simple equality check
		if (child.equals(parent))
			return true;
		// Look at array element type
		boolean bothArrays = child.getSort() == Type.ARRAY && parent.getSort() == Type.ARRAY;
		if (bothArrays) {
			// TODO: With usage cases of "isSubTypeOf(...)" should we just check the element types are equals?
			//  - Or should sub-typing with array element types be used like it currently is?
			child = child.getElementType();
			parent = parent.getElementType();
			// Dimensions must match, unless both are Object
			if (child.getDimensions() != parent.getDimensions() &&
					!(child.equals(OBJECT_TYPE) && parent.equals(OBJECT_TYPE)))
				return false;
		}
		// Null check in case
		if (parent == null)
			return false;
		// Treat lesser primitives as integers.
		//  - Because of boolean consts are ICONST_0/ICONST_1
		//  - Short parameters take the stack value of BIPUSH (int)
		if (parent.getSort() >= Type.BOOLEAN && parent.getSort() <= Type.INT)
			parent = Type.INT_TYPE;
		// Check for primitives
		//  - ASM sorts are in a specific order
		//  - If the expected sort is a larger type (greater sort) then the given type can
		//    be assumed to be compatible.
		if (isPrimitive(parent) && isPrimitive(child))
			return parent.getSort() >= child.getSort();
		// Use a simplified check if the expected type is just "Object"
		//  - Most things can be lumped into an object
		if (!isPrimitive(child) && parent.getDescriptor().equals("Ljava/lang/Object;"))
			return true;
		// Check if types are compatible
		if (child.getSort() == parent.getSort()) {
			AbstractValue host = AbstractValue.ofDefault(null, typeChecker, parent);
			return host != null && host.canMerge(AbstractValue.ofDefault(null, typeChecker, child));
		}
		return false;
	}

	/**
	 * @param type
	 * 		Some type.
	 *
	 * @return {@code true} for primitive types. {@code false} for object and array types.
	 */
	public static boolean isPrimitive(Type type) {
		return type.getSort() < Type.ARRAY;
	}

	static {
		// 0
		SORT_ORDER.add(Type.VOID);
		// 1
		SORT_ORDER.add(Type.BOOLEAN);
		// 8
		SORT_ORDER.add(Type.BYTE);
		// 16
		SORT_ORDER.add(Type.SHORT);
		SORT_ORDER.add(Type.CHAR);
		// 32
		SORT_ORDER.add(Type.INT);
		SORT_ORDER.add(Type.FLOAT);
		// 64
		SORT_ORDER.add(Type.DOUBLE);
		SORT_ORDER.add(Type.LONG);
		// ?
		SORT_ORDER.add(Type.ARRAY);
		SORT_ORDER.add(Type.OBJECT);
	}
}
