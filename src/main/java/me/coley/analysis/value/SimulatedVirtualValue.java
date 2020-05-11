package me.coley.analysis.value;

import me.coley.analysis.StaticInvokeFactory;
import me.coley.analysis.TypeChecker;
import me.coley.analysis.exception.SimFailedException;
import me.coley.analysis.util.TypeUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static me.coley.analysis.util.CollectUtils.combine;
import static me.coley.analysis.util.CollectUtils.distinct;

/**
 * Value recording the type and value<i>(using reflection and other means to track the existing value)</i>.
 *
 * @author Matt
 */
public class SimulatedVirtualValue extends VirtualValue {
	private static final Map<String, BiFunction<List<AbstractInsnNode>, TypeChecker, SimulatedVirtualValue>>
			TYPE_PRODUCERS = new HashMap<>();
	private static final String[][] BLACKLISTED_METHODS = {
		{"wait", "()V"},
		{"wait", "(J)V"},
		{"wait", "(JI)V"},
		{"notify", "()V"},
		{"notifyAll", "()V"},
		{"intern", "()Ljava/lang/String;"}
	};
	private static final Set<String> WHITELISTED_CLASSES = new HashSet<>(Arrays.asList(
		"java/lang/Long",
		"java/lang/Integer",
		"java/lang/Short",
		"java/lang/Character",
		"java/lang/Byte",
		"java/lang/Boolean",
		"java/lang/Float",
		"java/lang/Double",
		"java/lang/Math"
	));
	private Object[] currentValue;

	protected SimulatedVirtualValue(List<AbstractInsnNode> insns, Type type, Object value, TypeChecker typeChecker) {
		this(insns, type, value, new Object[]{value}, typeChecker);
	}

	protected SimulatedVirtualValue(List<AbstractInsnNode> insns, Type type, Object value, Object[] currentValue, TypeChecker typeChecker) {
		super(insns, type, copyValue(value), typeChecker);
		this.currentValue = currentValue;
		this.currentValue[0] = value;
	}

	/**
	 * @param type
	 * 		Some type.
	 *
	 * @return {@code true} when the type is supported.
	 */
	public static boolean supported(Type type) {
		return TYPE_PRODUCERS.containsKey(type.getInternalName());
	}

	/**
	 * Create a new simulation object for the given type.
	 *
	 * @param insns
	 * 		Instructions of value.
	 * @param typeChecker
	 * 		Type checker for comparison against other types.
	 * @param type
	 * 		Some type.
	 *
	 * @return New instance of type.
	 */
	public static SimulatedVirtualValue initialize(List<AbstractInsnNode> insns, TypeChecker typeChecker, Type type) {
		return TYPE_PRODUCERS.get(type.getInternalName()).apply(insns, typeChecker);
	}

	/**
	 * @param insn
	 * 		Instruction of value.
	 * @param typeChecker
	 * 		Type checker for comparison against other types.
	 * @param value
	 * 		String.
	 *
	 * @return String value.
	 */
	public static SimulatedVirtualValue ofString(AbstractInsnNode insn, TypeChecker typeChecker, String value) {
		return ofString(Collections.singletonList(insn), typeChecker, value);
	}

	/**
	 * @param insns
	 * 		Instructions of value.
	 * @param typeChecker
	 * 		Type checker for comparison against other types.
	 * @param value
	 * 		String.
	 *
	 * @return String value.
	 */
	public static SimulatedVirtualValue ofString(List<AbstractInsnNode> insns, TypeChecker typeChecker, String value) {
		return new SimulatedVirtualValue(insns, Type.getObjectType("java/lang/String"), value, typeChecker);
	}

	@Override
	public boolean isValueResolved() {
		return value != null;
	}

	/**
	 * @param factory
	 * 		Factory used to provide values. May be {@code null}. If
	 * @param insn
	 * 		Method invoke instruction.
	 * @param arguments
	 * 		Argument values.
	 * @param typeChecker
	 *      Type checker for comparison against other types.
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
		} catch(Throwable t) {
			throw new SimFailedException(insn, "Failed to invoke method", t);
		}
	}

	/**
	 * @param insn
	 * 		Method invoke instruction.
	 * @param arguments
	 * 		Argument values
	 *
	 * @return Act on the current reference.
	 */
	public AbstractValue ofVirtualInvoke(MethodInsnNode insn, List<? extends AbstractValue> arguments)
			throws SimFailedException {
		// Don't act on 'null' values
		if (isNull())
			throw new SimFailedException(insn, "Cannot act on null reference value");
		// Don't try to do object stuff with non-objects
		if (isPrimitive())
			throw new SimFailedException(insn, "Cannot act on a primitive");
		// Nullify voids
		Type desc = Type.getMethodType(insn.desc);
		if (desc.equals(Type.VOID_TYPE))
			return null;
		// Validate method context and arguments
		if (value == null)
			throw new SimFailedException(insn, "Context is null");
		if (arguments.stream().anyMatch(AbstractValue::isValueUnresolved))
			throw new SimFailedException(insn, "One or more arguments are not resolved");
		// Create new value from invoke
		try {
			return invokeVirtual(insn, insn.name, desc, arguments, currentValue[0]);
		} catch(Throwable t) {
			throw new SimFailedException(insn, "Failed to invoke method", t);
		}
	}

	/**
	 * @param min
	 * 		Method instruction.
	 * @param name
	 * 		Method name.
	 * @param desc
	 * 		Method type descriptor.
	 * @param invokeHost
	 * 		Object instance to invoke on.
	 *
	 * @return New value holder containing the new value from invoking the method.
	 *
	 * @throws ReflectiveOperationException
	 * 		When the target method could not be invoked.
	 */
	private AbstractValue invokeVirtual(MethodInsnNode min, String name,
										Type desc, List<? extends AbstractValue> arguments,
										Object invokeHost) throws ReflectiveOperationException {
		// Check against constructors
		Type[] argTypes = desc.getArgumentTypes();
		if (name.equals("<init>")) {
			for (Constructor<?> c : invokeHost.getClass().getConstructors()) {
				if (c.getParameterCount() != argTypes.length)
					continue;
				boolean argsMatch = true;
				for(int i = 0; i < argTypes.length; i++)
					argsMatch &= argTypes[i].equals(Type.getType(c.getParameterTypes()[i]));
				if (argsMatch) {
					List<AbstractInsnNode> insns = distinct(combine(arguments.stream()
							.flatMap(arg -> arg.getInsns().stream())
							.collect(Collectors.toList()), getInsns(), min));
					Object[] argValues = arguments.stream()
							.map(AbstractValue::getValue).toArray();
					c.setAccessible(true);
					Object retVal = c.newInstance(argValues);
					return new SimulatedVirtualValue(insns,
							Type.getType(retVal.getClass()), retVal, typeChecker);
				}
			}
		}
		// Check against blacklist. They are do-nothing methods that we want to skip.
		for (String[] def : BLACKLISTED_METHODS)
			if (def[0].equals(name) && def[1].equals(desc.getDescriptor()))
				return this;
		// Check against normal methods
		Type retType = desc.getReturnType();
		for(Method mm : invokeHost.getClass().getMethods()) {
			// Skip non-matching methods
			if (!mm.getName().equals(name))
				continue;
			if (mm.getParameterCount() != argTypes.length)
				continue;
			if (isStatic(mm.getModifiers()))
				continue;
			boolean argsMatch = true;
			for(int i = 0; i < argTypes.length; i++)
				argsMatch &= argTypes[i].equals(Type.getType(mm.getParameterTypes()[i]));
			// Invoke if matched name/args.
			if (argsMatch) {
				Object[] argValues = arguments.stream()
						.map(AbstractValue::getValue).toArray();
				mm.setAccessible(true);
				Object retVal = mm.invoke(invokeHost, argValues);
				// Check void types.
				if (retType.getSort() == Type.VOID)
					return null;
				// Handle return value.
				if (retVal != null) {
					List<AbstractInsnNode> insns = distinct(combine(arguments.stream()
							.flatMap(arg -> arg.getInsns().stream())
							.collect(Collectors.toList()), getInsns(), min));
					if (TypeUtil.isPrimitiveDesc(retType.getDescriptor())) {
						// Unbox primitive wrappers if descriptor calls for it.
						return unboxed(insns, retVal);
					}  else {
						// Not a primitive
						return new SimulatedVirtualValue(insns, Type.getType(retVal.getClass()), retVal, currentValue, typeChecker);
					}
				}
			}
		}
		// Invoke didn't occur, throw exception rather than return null
		throw new IllegalStateException("Could not find method to simulate: " +
				type.getInternalName() + "." +  name + desc);
	}

	@Override
	public AbstractValue copy(AbstractInsnNode insn) {
		return new SimulatedVirtualValue(combine(getInsns(), insn), getType(), getValue(), currentValue, typeChecker);
	}

	/**
	 * @param min
	 * 		Method instruction.
	 * @param name
	 * 		Method name.
	 * @param desc
	 * 		Method type descriptor.
	 * @param arguments
	 * 		Argument values
	 * @param typeChecker
	 * 		Type checker for comparison against other types.
	 *
	 * @return New value holder containing the new value from invoking the method.
	 *
	 * @throws ReflectiveOperationException
	 * 		When the target method could not be invoked.
	 */
	private static AbstractValue invokeStatic(MethodInsnNode min, String owner, String name, Type desc,
											  List<? extends AbstractValue> arguments, TypeChecker typeChecker)
			throws ReflectiveOperationException {
		Class<?> cls = Class.forName(owner.replace('/', '.'));
		Type retType = desc.getReturnType();
		Type[] argTypes = desc.getArgumentTypes();
		for(Method mm : cls.getMethods()) {
			// Skip non-matching methods
			if (!mm.getName().equals(name))
				continue;
			if (mm.getParameterCount() != argTypes.length)
				continue;
			if (!isStatic(mm.getModifiers()))
				continue;
			boolean argsMatch = true;
			for(int i = 0; i < argTypes.length; i++)
				argsMatch &= argTypes[i].equals(Type.getType(mm.getParameterTypes()[i]));
			// Invoke if matched name/args.
			if (argsMatch) {
				Object[] argValues = arguments.stream().map(AbstractValue::getValue).toArray();
				mm.setAccessible(true);
				Object retVal = mm.invoke(null, argValues);
				// Check void types.
				if (retType.getSort() == Type.VOID)
					return null;
				// Handle return value.
				if (retVal != null) {
					List<AbstractInsnNode> insns = distinct(combine(arguments.stream()
							.flatMap(arg -> arg.getInsns().stream())
							.collect(Collectors.toList()), min));
					if (TypeUtil.isPrimitiveDesc(retType.getDescriptor())) {
						// Unbox primitive wrappers if descriptor calls for it.
						return unboxed(insns, retVal);
					}  else {
						// Not a primitive
						return new SimulatedVirtualValue(insns,
								Type.getType(retVal.getClass()), retVal, typeChecker);
					}
				}

			}
		}
		// Invoke didn't occur, throw exception rather than return null
		throw new IllegalStateException("Could not find method to simulate: " +
				owner + "." +  name + desc);
	}

	/**
	 * @param insns
	 * 		Instructions contributing to the method called.
	 * @param retVal
	 * 		Boxed primitive.
	 *
	 * @return Value wrapper of primitive.
	 */
	private static AbstractValue unboxed(List<AbstractInsnNode> insns, Object retVal) {
		if (retVal instanceof Integer || retVal instanceof Short || retVal instanceof Byte)
			return PrimitiveValue.ofInt(insns, ((Number) retVal).intValue());
		else if (retVal instanceof Float)
			return PrimitiveValue.ofFloat(insns, ((Float) retVal));
		else if (retVal instanceof Double)
			return PrimitiveValue.ofDouble(insns, ((Double) retVal));
		else if (retVal instanceof Boolean)
			return PrimitiveValue.ofInt(insns, ((Boolean) retVal) ? 1 : 0);
		else if (retVal instanceof Character)
			return PrimitiveValue.ofChar(insns, (Character) retVal);
		else if (retVal instanceof Long)
			return PrimitiveValue.ofLong(insns, (Long) retVal);
		throw new UnsupportedOperationException("Unsupported boxed type: " + retVal.getClass().getName());
	}

	/**
	 * @return New value instance copied from current {@link #value}.
	 */
	private static Object copyValue(Object value) {
		if (value instanceof String) {
			return value.toString();
		} else if (value instanceof StringBuilder) {
			return new StringBuilder(value.toString());
		}  else if (value instanceof StringBuffer) {
			return new StringBuffer(value.toString());
		}
		throw new UnsupportedOperationException(value.getClass() + " copying not supported");
	}

	private static boolean isStaticMethodWhitelisted(String owner, String name, String desc) {
		return WHITELISTED_CLASSES.contains(owner);
	}

	private static boolean isStatic(int modifiers) {
		return (modifiers & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC;
	}

	static {
		TYPE_PRODUCERS.put("java/lang/StringBuilder", (insns, typeChecker) ->
				new SimulatedVirtualValue(insns, Type.getObjectType("java/lang/StringBuilder"), new StringBuilder(), typeChecker));
		TYPE_PRODUCERS.put("java/lang/StringBuffer", (insns, typeChecker) ->
				new SimulatedVirtualValue(insns, Type.getObjectType("java/lang/StringBuffer"), new StringBuffer(), typeChecker));
		TYPE_PRODUCERS.put("java/lang/String", (insns, typeChecker) -> ofString(insns, typeChecker, ""));
	}
}
