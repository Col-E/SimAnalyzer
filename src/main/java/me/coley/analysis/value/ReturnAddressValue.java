package me.coley.analysis.value;

import org.objectweb.asm.Type;

/**
 * Value wrapper for return addresses.
 *
 * @author Matt
 */
public class ReturnAddressValue extends AbstractValue {
	public static final ReturnAddressValue RETURN_ADDRESS_VALUE = new ReturnAddressValue();

	private ReturnAddressValue() {
		super(Type.VOID_TYPE, null);
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
