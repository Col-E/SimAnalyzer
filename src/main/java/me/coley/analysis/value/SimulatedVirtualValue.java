package me.coley.analysis.value;

import me.coley.analysis.exception.SimFailedException;
import me.coley.analysis.util.TypeUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Value recording the type and value<i>(using reflection and other means to track the existing value)</i>.
 *
 * @author Matt
 */
public class SimulatedVirtualValue extends VirtualValue {
	private static final Map<String, Supplier<SimulatedVirtualValue>> TYPE_PRODUCERS = new HashMap<>();
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

	protected SimulatedVirtualValue(Type type, Object value) {
		this(type, value, new Object[]{value});
	}

	protected SimulatedVirtualValue(Type type, Object value, Object[] currentValue) {
		super(type, copyValue(value));
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
	 * @param type
	 * 		Some type.
	 *
	 * @return New instance of type.
	 */
	public static SimulatedVirtualValue initialize(Type type) {
		return TYPE_PRODUCERS.get(type.getInternalName()).get();
	}

	/**
	 * @param value
	 * 		String.
	 *
	 * @return String value.
	 */
	public static SimulatedVirtualValue ofString(String value) {
		return new SimulatedVirtualValue(Type.getObjectType("java/lang/String"), value);
	}

	@Override
	public boolean isValueResolved() {
		return value != null;
	}

	/**
	 * @param insn
	 * 		Method invoke instruction.
	 * @param arguments
	 * 		Argument values
	 *
	 * @return New instance from static method invoke.<br><b>Will be {@code null} if the method
	 * could not be invoked</b>.
	 */
	public static AbstractValue ofStaticInvoke(MethodInsnNode insn, List<? extends AbstractValue> arguments)
			throws SimFailedException {
		String owner = insn.owner;
		String name = insn.name;
		String desc = insn.desc;
		if (!isStaticMethodWhitelisted(owner, name, desc))
			throw new SimFailedException(insn, "Static method is not whitelisted.");
		try {
			return invokeStatic(owner, name, Type.getMethodType(desc),
					arguments.stream().map(AbstractValue::getValue).toArray());
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
			return invokeVirtual(insn.name, desc, arguments.stream()
					.map(AbstractValue::getValue).toArray(), currentValue[0]);
		} catch(Throwable t) {
			throw new SimFailedException(insn, "Failed to invoke method", t);
		}
	}

	/**
	 * @param name
	 * 		Method name.
	 * @param desc
	 * 		Method type descriptor.
	 * @param argValues
	 * 		Argument values
	 * @param invokeHost
	 * 		Object instance to invoke on.
	 *
	 * @return New value holder containing the new value from invoking the method.
	 *
	 * @throws ReflectiveOperationException
	 * 		When the target method could not be invoked.
	 */
	private AbstractValue invokeVirtual(String name, Type desc, Object[] argValues, Object invokeHost)
			throws ReflectiveOperationException {
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
					c.setAccessible(true);
					Object retVal = c.newInstance(argValues);
					return new SimulatedVirtualValue(Type.getType(retVal.getClass()), retVal);
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
				mm.setAccessible(true);
				Object retVal = mm.invoke(invokeHost, argValues);
				// Check void types.
				if (retType.getSort() == Type.VOID)
					return null;
				// Handle return value.
				if (retVal != null) {
					if (TypeUtil.isPrimitiveDesc(retType.getDescriptor())) {
						// Unbox primitive wrappers if descriptor calls for it.
						return unboxed(retVal);
					}  else {
						// Not a primitive
						return new SimulatedVirtualValue(Type.getType(retVal.getClass()), retVal, currentValue);
					}
				}
			}
		}
		// Invoke didn't occur, throw exception rather than return null
		throw new IllegalStateException("Could not find method to simulate: " +
				type.getInternalName() + "." +  name + desc);
	}

	/**
	 * @param name
	 * 		Method name.
	 * @param desc
	 * 		Method type descriptor.
	 * @param argValues
	 * 		Argument values
	 *
	 * @return New value holder containing the new value from invoking the method.
	 *
	 * @throws ReflectiveOperationException
	 * 		When the target method could not be invoked.
	 */
	private static AbstractValue invokeStatic(String owner, String name, Type desc, Object[] argValues)
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
				mm.setAccessible(true);
				Object retVal = mm.invoke(null, argValues);
				// Check void types.
				if (retType.getSort() == Type.VOID)
					return null;
				// Handle return value.
				if (retVal != null) {
					if (TypeUtil.isPrimitiveDesc(retType.getDescriptor())) {
						// Unbox primitive wrappers if descriptor calls for it.
						return unboxed(retVal);
					}  else {
						// Not a primitive
						return new SimulatedVirtualValue(Type.getType(retVal.getClass()), retVal);
					}
				}

			}
		}
		// Invoke didn't occur, throw exception rather than return null
		throw new IllegalStateException("Could not find method to simulate: " +
				owner + "." +  name + desc);
	}

	/**
	 * @param retVal
	 * 		Boxed primitive.
	 *
	 * @return Value wrapper of primitive.
	 */
	private static AbstractValue unboxed(Object retVal) {
		if (retVal instanceof Integer || retVal instanceof Short || retVal instanceof Byte)
			return PrimitiveValue.ofInt(((Number) retVal).intValue());
		else if (retVal instanceof Float)
			return PrimitiveValue.ofFloat(((Float) retVal));
		else if (retVal instanceof Double)
			return PrimitiveValue.ofDouble(((Double) retVal));
		else if (retVal instanceof Boolean)
			return PrimitiveValue.ofInt(((Boolean) retVal) ? 1 : 0);
		else if (retVal instanceof Character)
			return PrimitiveValue.ofChar((Character) retVal);
		else if (retVal instanceof Long)
			return PrimitiveValue.ofLong((Long) retVal);
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
		TYPE_PRODUCERS.put("java/lang/StringBuilder",
				() -> new SimulatedVirtualValue(Type.getObjectType("java/lang/StringBuilder"),
						new StringBuilder()));
		TYPE_PRODUCERS.put("java/lang/StringBuffer",
				() -> new SimulatedVirtualValue(Type.getObjectType("java/lang/StringBuffer"),
						new StringBuffer()));
		TYPE_PRODUCERS.put("java/lang/String", () -> ofString(""));
	}
}
