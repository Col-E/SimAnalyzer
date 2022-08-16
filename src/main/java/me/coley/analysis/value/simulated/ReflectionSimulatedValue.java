package me.coley.analysis.value.simulated;

import me.coley.analysis.StaticInvokeFactory;
import me.coley.analysis.TypeChecker;
import me.coley.analysis.exception.SimFailedException;
import me.coley.analysis.util.GetSet;
import me.coley.analysis.value.AbstractValue;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.List;

/**
 * Value recording the type and value<i>(using reflection and other means to track the existing value)</i>.
 *
 * @author Matt Coley
 */
public class ReflectionSimulatedValue extends AbstractSimulatedValue<Object> {
	protected ReflectionSimulatedValue(List<AbstractInsnNode> insns, Type type, Object value, TypeChecker typeChecker) {
		super(insns, type, value, typeChecker);
	}

	protected ReflectionSimulatedValue(List<AbstractInsnNode> insns, Type type, Object value,
									   GetSet<Object> resultValue, TypeChecker typeChecker) {
		super(insns, type, value, resultValue, typeChecker);
	}

	@Override
	protected AbstractValue create(List<AbstractInsnNode> collection) {
		return new ReflectionSimulatedValue(collection, getType(), getValue(), resultValue, typeChecker);
	}

	/**
	 * First, the factory is checked to see if it yields a value. If not, then the default static handling is invoked.
	 *
	 * @param factory
	 * 		Factory used to provide values. May be {@code null}. If
	 * @param insn
	 * 		Method invoke instruction.
	 * @param arguments
	 * 		Argument values.
	 * @param typeChecker
	 * 		Type checker for comparison against other types.
	 *
	 * @return New instance from static method invoke.<br><b>Will be {@code null} if the method
	 * could not be invoked</b>.
	 */
	public static AbstractValue ofStaticInvoke(StaticInvokeFactory factory, MethodInsnNode insn,
											   List<? extends AbstractValue> arguments, TypeChecker typeChecker)
			throws SimFailedException {
		String owner = insn.owner;
		String name = insn.name;
		String desc = insn.desc;
		if (factory != null)
			return factory.invokeStatic(insn, arguments);
		else if (!isStaticMethodWhitelisted(owner, name, desc))
			throw new SimFailedException(insn, "Static method is not whitelisted.");
		try {
			return invokeStatic(insn, owner, name, Type.getMethodType(desc),
					arguments, typeChecker);
		} catch (Throwable t) {
			throw new SimFailedException(insn, "Failed to invoke method", t);
		}
	}

	@Override
	public AbstractValue ofVirtualInvoke(MethodInsnNode min, List<? extends AbstractValue> arguments)
			throws SimFailedException {
		return defaultOfVirtualInvoke(min, arguments);
	}
}
