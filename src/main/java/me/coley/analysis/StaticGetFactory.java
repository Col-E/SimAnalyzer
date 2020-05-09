package me.coley.analysis;

import me.coley.analysis.value.AbstractValue;
import org.objectweb.asm.Opcodes;

/**
 * Factory for generating {@link AbstractValue} from static field references.
 *
 * @author Matt
 */
public class StaticGetFactory {
	/**
	 * @param owner
	 * 		Field owner.
	 * @param name
	 * 		Field name.
	 * @param desc
	 * 		Field descriptor.
	 *
	 * @return Value of {@link Opcodes#GETSTATIC}. {@link Unresolved} for unknown values.
	 */
	public AbstractValue getStatic(String owner, String name, String desc) {
		return null;
	}
}
