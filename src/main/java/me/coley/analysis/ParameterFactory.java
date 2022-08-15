package me.coley.analysis;

import me.coley.analysis.value.AbstractValue;
import org.objectweb.asm.Type;

/**
 * Factory for generating {@link AbstractValue} for method parameter values.
 * These values populate the initial frame of the analysis process.
 *
 * @author Matt Coley
 */
public interface ParameterFactory {
	/**
	 * @param isInstanceMethod
	 *        {@code false} when the method is static.
	 * @param local
	 * 		Local variable index.
	 * @param type
	 * 		Local variable type.
	 *
	 * @return Parameter value.
	 */
	AbstractValue createParameterValue(boolean isInstanceMethod, int local, Type type);
}
