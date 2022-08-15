package me.coley.analysis;

import org.objectweb.asm.Type;

/**
 * Used to assist the analyzer to figure out what common types should be.
 *
 * @author Matt Coley
 */
public interface TypeResolver {
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
