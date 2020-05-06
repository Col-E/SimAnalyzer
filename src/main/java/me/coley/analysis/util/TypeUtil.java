package me.coley.analysis.util;

import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for ASM's {@link Type} class <i>(And some additional descriptor cases)</i>
 *
 * @author Matt
 */
public class TypeUtil {

	private static final List<Integer> SORT_ORDER = new ArrayList<>();
	/**
	 * Cosntant for object type.
	 */
	public static final Type OBJECT_TYPE = Type.getObjectType("java/lang/Object");

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
