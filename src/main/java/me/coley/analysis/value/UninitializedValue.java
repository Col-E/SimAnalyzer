package me.coley.analysis.value;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.List;

/**
 * Value wrapper for uninitialized values.
 *
 * @author Matt Coley
 */
public class UninitializedValue extends AbstractValue {
	public static final AbstractValue UNINITIALIZED_VALUE = new UninitializedValue(null, null);

	private UninitializedValue(Type type, Object value) {
		super((List<AbstractInsnNode>) null, type, value);
	}

	@Override
	protected UninitializedValue create(List<AbstractInsnNode> collection) {
		throw new IllegalStateException("Copying an uninitialized value should not occur!");
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
