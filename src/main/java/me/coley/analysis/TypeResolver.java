package me.coley.analysis;

import org.objectweb.asm.Type;

/**
 * Used to assist the analyzer to figure out what common types should be.
 *
 * @author Matt Coley
 */
public interface TypeResolver {
	/**
	 * Given {@code List.class.isAssignableFrom(ArrayList.class)} the {@code first} parameter would be
	 * {@code java/util/List} and the {@code second} parameter would be {@code java/util/ArrayList}.
	 *
	 * @param first
	 * 		Assumed super-class or interface type.
	 * @param second
	 * 		Assumed child class which extends the super-class or implements the interface type.
	 *
	 * @return {@code true} when {@code first.isAssignableFrom(second)}.
	 */
	boolean isAssignableFrom(Type first, Type second);

	/**
	 * @param type1
	 * 		Some type.
	 * @param type2
	 * 		Some type.
	 *
	 * @return The common parent type.
	 */
	Type common(Type type1, Type type2);

	/**
	 * An edge case of {@link #common(Type, Type)} that can assume the common type is an exception or throwable type.
	 *
	 * @param type1
	 * 		Some exception type.
	 * @param type2
	 * 		Some exception type.
	 *
	 * @return The common parent type.
	 */
	Type commonException(Type type1, Type type2);
}
