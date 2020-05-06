package me.coley.analysis.value;

import me.coley.analysis.util.TypeUtil;

/**
 * Value wrapper for null constants.
 *
 * @author Matt
 */
public class NullConstantValue extends AbstractValue {
	public static final NullConstantValue NULL_VALUE = new NullConstantValue();

	protected NullConstantValue() {
		super(TypeUtil.OBJECT_TYPE, null);
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
		return true;
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
