package me.coley.analysis.value;

import me.coley.analysis.TypeResolver;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.List;

/**
 * Value wrapper recording the type. Wrapped value is a placeholder and used to denote an unresolved value.
 *
 * @author Matt Coley
 */
public class VirtualValue extends AbstractValue {
	protected final TypeResolver typeResolver;

	protected VirtualValue(AbstractInsnNode insn, Type type, Object value, TypeResolver typeResolver) {
		super(insn, type, value);
		this.typeResolver = typeResolver;
	}

	protected VirtualValue(List<AbstractInsnNode> insns, Type type, Object value, TypeResolver typeResolver) {
		super(insns, type, value);
		this.typeResolver = typeResolver;
	}

	/**
	 * @param insn
	 * 		The instruction of this value.
	 * @param typeResolver
	 * 		Type resolver for comparison against other types.
	 * @param type
	 * 		Type to virtualize.
	 * @param value
	 * 		Value to use.
	 *
	 * @return Virtual value of type.
	 */
	public static VirtualValue ofVirtual(AbstractInsnNode insn, TypeResolver typeResolver, Type type, Object value) {
		return new VirtualValue(insn, type, value, typeResolver);
	}

	/**
	 * @param insns
	 * 		The instructions of this value.
	 * @param typeResolver
	 * 		Type resolver for comparison against other types.
	 * @param type
	 * 		Type to virtualize.
	 * @param value
	 * 		Value to use.
	 *
	 * @return Virtual value of type.
	 */
	public static VirtualValue ofVirtual(List<AbstractInsnNode> insns, TypeResolver typeResolver, Type type, Object value) {
		return new VirtualValue(insns, type, value, typeResolver);
	}

	/**
	 * @param insn
	 * 		The instruction of this value.
	 * @param typeResolver
	 * 		Type resolver for comparison against other types.
	 * @param type
	 * 		Type to virtualize.
	 *
	 * @return Virtual value of type.
	 */
	public static VirtualValue ofVirtual(AbstractInsnNode insn, TypeResolver typeResolver, Type type) {
		return new VirtualValue(insn, type, new Unresolved(type), typeResolver);
	}

	/**
	 * @param insns
	 * 		The instructions of this value.
	 * @param typeResolver
	 * 		Type resolver for comparison against other types.
	 * @param type
	 * 		Type to virtualize.
	 *
	 * @return Virtual value of type.
	 */
	public static VirtualValue ofVirtual(List<AbstractInsnNode> insns, TypeResolver typeResolver, Type type) {
		return new VirtualValue(insns, type, new Unresolved(type), typeResolver);
	}

	/**
	 * @param insn
	 * 		The instruction of this value.
	 * @param typeResolver
	 * 		Type resolver for comparison against other types.
	 * @param value
	 * 		The value / type of class.
	 *
	 * @return Class value.
	 */
	public static VirtualValue ofClass(AbstractInsnNode insn, TypeResolver typeResolver, Type value) {
		return new VirtualValue(insn, Type.getObjectType("java/lang/Class"), value, typeResolver);
	}

	/**
	 * @param insn
	 * 		The instruction of this value.
	 * @param typeResolver
	 * 		Type resolver for comparison against other types.
	 * @param desc
	 * 		Method type descriptor.
	 *
	 * @return Act on the current reference.
	 */
	public AbstractValue ofMethodRef(AbstractInsnNode insn, TypeResolver typeResolver, Type desc) {
		// Validate desc
		if (desc == null)
			throw new IllegalStateException("Method descriptor must not be null");

		// Don't act on 'null' values
		if (value == null || value.equals(Type.VOID_TYPE))
			throw new IllegalStateException("Cannot act on null reference value");

		// Don't try to do object stuff with non-objects
		if (!isReference())
			throw new IllegalStateException("Cannot act on reference on non-reference value");

		// Handle return types
		Type retType = desc.getReturnType();
		if (retType.equals(Type.VOID_TYPE))
			return null;
		if (retType.getSort() <= Type.DOUBLE)
			return new PrimitiveValue(insn, retType);
		return ofVirtual(insn, typeResolver, retType);
	}

	@Override
	protected AbstractValue create(List<AbstractInsnNode> collection) {
		return new VirtualValue(collection, getType(), getValue(), typeResolver);
	}

	@Override
	public boolean canMerge(AbstractValue other) {
		if (other == this)
			return true;
		else if (other instanceof NullConstantValue ||
				other == UninitializedValue.UNINITIALIZED_VALUE ||
				other == null)
			return false;
		else if (type == null)
			return other.type == null;
		return type.equals(other.type) || isParent(type, other.type);
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
		return false;
	}

	@Override
	public boolean equals(Object other) {
		if (other == this)
			return true;
		else if (other == UninitializedValue.UNINITIALIZED_VALUE)
			return false;
		else if (other instanceof NullConstantValue)
			return false;
		else if (other instanceof VirtualValue) {
			VirtualValue rOther = (VirtualValue) other;
			if (value instanceof StringBuilder || value instanceof StringBuffer)
				return value.toString().equals(rOther.value.toString());
			else
				return type.equals(rOther.type) && value.equals(rOther.value);
		}
		return false;
	}

	protected boolean isParent(Type parent, Type child) {
		if (parent == null || child == null)
			throw new IllegalStateException("Cannot find common type of parent null type");
		else if (parent.equals(child))
			return true;
		else if (parent.getSort() == Type.OBJECT && child.getSort() == Type.OBJECT) {
			if (parent.equals(child))
				return true;
			return typeResolver.isAssignableFrom(parent, child);
		} else
			return parent.getSort() < Type.ARRAY && child.getSort() < Type.ARRAY;
	}
}
