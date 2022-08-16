package me.coley.analysis.value;

import org.objectweb.asm.Type;

import java.util.Objects;

/**
 * Placeholder value for an unresolved value of some type.
 *
 * @author Matt Coley
 */
public class Unresolved {
	private final Type type;

	/**
	 * @param type Type of virtualized object.
	 */
	public Unresolved(Type type) {
		this.type = type;
	}

	/**
	 * @return {@code true} when wirtualized object is an array.
	 */
	public boolean isArray() {
		return type.getSort() == Type.ARRAY;
	}

	@Override
	public String toString() {
		return type.getInternalName();
	}

	@Override
	public int hashCode() {
		return Objects.hash(type);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Unresolved))
			return false;
		return Objects.equals(type, ((Unresolved) o).type);
	}
}
