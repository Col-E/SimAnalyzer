package me.coley.analysis.value;

import org.objectweb.asm.Type;

/**
 * Value wrapper for uninitialized values.
 *
 * @author Matt
 */
public class UninitializedValue extends AbstractValue {
	public static final AbstractValue UNINITIALIZED_VALUE = new UninitializedValue(null, null);

	private UninitializedValue(Type type, Object value) {
		super(type, value);
	}

	@Override
	public boolean canMerge(AbstractValue other) {
		return other == this;
	}

	@Override
	public boolean isPrimitive() {
		return false;
	}

	@Override
	public boolean isReference() {
		return false;
	}

	@Override
	public boolean isValueResolved() {
		return true;
	}

	@Override
	public boolean equals(Object other) {
		return other == this;
	}
}
