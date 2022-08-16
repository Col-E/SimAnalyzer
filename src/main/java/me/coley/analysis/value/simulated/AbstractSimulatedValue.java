package me.coley.analysis.value.simulated;

import me.coley.analysis.TypeChecker;
import me.coley.analysis.exception.SimFailedException;
import me.coley.analysis.util.GetSet;
import me.coley.analysis.util.TypeUtil;
import me.coley.analysis.value.AbstractValue;
import me.coley.analysis.value.PrimitiveValue;
import me.coley.analysis.value.VirtualValue;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static me.coley.analysis.util.CollectUtils.*;

/**
 * Base logic of simulated value types.
 *
 * @param <T>
 * 		Type of value simulated.
 *
 * @author Matt Coley
 */
public abstract class AbstractSimulatedValue<T> extends VirtualValue {
	private static final Map<String, BiFunction<List<AbstractInsnNode>, TypeChecker, AbstractSimulatedValue<?>>>
			TYPE_PRODUCERS = new HashMap<>();
	protected static final String[][] BLACKLISTED_METHODS = {
		{"wait", "()V"},
		{"wait", "(J)V"},
		{"wait", "(JI)V"},
		{"notify", "()V"},
		{"notifyAll", "()V"},
		{"intern", "()Ljava/lang/String;"}
	};
	protected static final Set<String> WHITELISTED_CLASSES = new HashSet<>(Arrays.asList(
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

	/**
	 * Where the {@link #value} is the value at the instruction,
	 * this is the <i>end-result value</i> after evaluation of the entire method.
	 * <br>
	 * If you step through the analysis process in a debugger, this will hold the current value.
	 */
	protected final GetSet<T> resultValue;

	protected AbstractSimulatedValue(List<AbstractInsnNode> insns, Type type, T value, TypeChecker typeChecker) {
		// Called to create a new chain of simulated values.
		this(insns, type, value, new GetSet<>(value), typeChecker);
	}

	protected AbstractSimulatedValue(List<AbstractInsnNode> insns, Type type, T value,
									 GetSet<T> resultValue, TypeChecker typeChecker) {
		// Called to add on to an existing chain of simulated values.
		super(insns, type, copyValue(value), typeChecker);
		this.resultValue = resultValue;
	}

	@Override
	public boolean isValueResolved() {
		return value != null;
	}

	/**
	 * @return Result value of the series of connected simulation frames.
	 */
	public T getResultValue() {
		return resultValue.get();
	}

	/**
	 * Externally update the result value.
	 *
	 * @param value
	 * 		New result value.
	 */
	public void updateResultValue(T value) {
		resultValue.set(value);
	}

	/**
	 * @param min
	 * 		Method being called on the current value.
	 * @param arguments
	 * 		Arguments passed.
	 *
	 * @return Return value of method.
	 *
	 * @throws SimFailedException
	 * 		When the method call could not be simulated.
	 */
	public abstract AbstractValue ofVirtualInvoke(MethodInsnNode min, List<? extends AbstractValue> arguments)
			throws SimFailedException;

	/**
	 * A default impl to use as a fallback of child implementations of {@link #ofVirtualInvoke(MethodInsnNode, List)}.
	 *
	 * @param min
	 * 		Method being called on the current value.
	 * @param arguments
	 * 		Arguments passed.
	 *
	 * @return Return value of method.
	 *
	 * @throws SimFailedException
	 * 		When the method call could not be simulated.
	 */
	protected AbstractValue defaultOfVirtualInvoke(MethodInsnNode min, List<? extends AbstractValue> arguments)
			throws SimFailedException {
		// Don't act on 'null' values
		if (isNull())
			throw new SimFailedException(min, "Cannot act on null reference value");
		// Don't try to do object stuff with non-objects
		if (isPrimitive())
			throw new SimFailedException(min, "Cannot act on a primitive");
		// Nullify voids
		Type desc = Type.getMethodType(min.desc);
		//  if (desc.getReturnType().equals(Type.VOID_TYPE))
		//  	return null;
		// Validate method context and arguments
		if (value == null)
			throw new SimFailedException(min, "Context is null");
		if (arguments.stream().anyMatch(AbstractValue::isValueUnresolved))
			throw new SimFailedException(min, "One or more arguments are not resolved");
		// Create new value from invoke
		try {
			return invokeVirtual(min, min.name, desc, arguments, resultValue.get());
		} catch (Throwable t) {
			throw new SimFailedException(min, "Failed to invoke method", t);
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
	@SuppressWarnings("unchecked")
	protected AbstractValue invokeVirtual(MethodInsnNode min, String name,
										  Type desc, List<? extends AbstractValue> arguments,
										  Object invokeHost) throws ReflectiveOperationException {
		// Check against constructors
		Type[] argTypes = desc.getArgumentTypes();
		if (name.equals("<init>")) {
			for (Constructor<?> c : invokeHost.getClass().getConstructors()) {
				if (c.getParameterCount() != argTypes.length)
					continue;
				boolean argsMatch = true;
				for (int i = 0; i < argTypes.length; i++)
					argsMatch &= argTypes[i].equals(Type.getType(c.getParameterTypes()[i]));
				if (argsMatch) {
					List<AbstractInsnNode> insns = distinct(combineAdd(arguments.stream()
							.flatMap(arg -> arg.getInsns().stream())
							.collect(Collectors.toList()), getInsns(), min));
					Object[] argValues = arguments.stream()
							.map(AbstractValue::getValue).toArray();
					c.setAccessible(true);
					Object retVal = c.newInstance(argValues);
					return new ReflectionSimulatedValue(insns,
							Type.getType(retVal.getClass()), retVal, typeChecker);
				}
			}
		}
		// Check against blacklist. They are do-nothing methods that we want to skip.
		for (String[] def : BLACKLISTED_METHODS)
			if (def[0].equals(name) && def[1].equals(desc.getDescriptor()))
				return new ReflectionSimulatedValue(insns,
						getType(), getValue(), typeChecker);
		// Check against normal methods
		Type retType = desc.getReturnType();
		for (Method mm : invokeHost.getClass().getMethods()) {
			// Skip non-matching methods
			if (!mm.getName().equals(name))
				continue;
			if (mm.getParameterCount() != argTypes.length)
				continue;
			if (isStatic(mm.getModifiers()))
				continue;
			boolean argsMatch = true;
			for (int i = 0; i < argTypes.length; i++)
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
					List<AbstractInsnNode> insns = distinct(combineAdd(arguments.stream()
							.flatMap(arg -> arg.getInsns().stream())
							.collect(Collectors.toList()), getInsns(), min));
					if (TypeUtil.isPrimitiveDesc(retType.getDescriptor())) {
						// Unbox primitive wrappers if descriptor calls for it.
						return unboxed(insns, retVal);
					} else {
						// Not a primitive
						return new ReflectionSimulatedValue(insns, Type.getType(retVal.getClass()), retVal,
								(GetSet<Object>) resultValue, typeChecker);
					}
				}
			}
		}
		// Invoke didn't occur, throw exception rather than return null
		throw new IllegalStateException("Could not find method to simulate: " +
				type.getInternalName() + "." + name + desc);
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
	protected static AbstractValue invokeStatic(MethodInsnNode min, String owner, String name, Type desc,
												List<? extends AbstractValue> arguments, TypeChecker typeChecker)
			throws ReflectiveOperationException {
		Class<?> cls = Class.forName(owner.replace('/', '.'));
		Type retType = desc.getReturnType();
		Type[] argTypes = desc.getArgumentTypes();
		for (Method mm : cls.getMethods()) {
			// Skip non-matching methods
			if (!mm.getName().equals(name))
				continue;
			if (mm.getParameterCount() != argTypes.length)
				continue;
			if (!isStatic(mm.getModifiers()))
				continue;
			boolean argsMatch = true;
			for (int i = 0; i < argTypes.length; i++)
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
					List<AbstractInsnNode> insns = distinct(add(arguments.stream()
							.flatMap(arg -> arg.getInsns().stream())
							.collect(Collectors.toList()), min));
					if (TypeUtil.isPrimitiveDesc(retType.getDescriptor())) {
						// Unbox primitive wrappers if descriptor calls for it.
						return unboxed(insns, retVal);
					} else {
						// Not a primitive
						return new ReflectionSimulatedValue(insns, Type.getType(retVal.getClass()), retVal, typeChecker);
					}
				}
			}
		}
		// Invoke didn't occur, throw exception rather than return null
		throw new IllegalStateException("Could not find method to simulate: " +
				owner + "." + name + desc);
	}

	protected static boolean isStaticMethodWhitelisted(String owner, String name, String desc) {
		return WHITELISTED_CLASSES.contains(owner);
	}

	protected static boolean isStatic(int modifiers) {
		return (modifiers & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC;
	}

	/**
	 * @param insns
	 * 		Instructions contributing to the method called.
	 * @param retVal
	 * 		Boxed primitive.
	 *
	 * @return Value wrapper of primitive.
	 */
	protected static AbstractValue unboxed(List<AbstractInsnNode> insns, Object retVal) {
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
	protected static Object copyValue(Object value) {
		if (value instanceof String) {
			return value.toString();
		} else if (value instanceof StringBuilder) {
			return new StringBuilder(value.toString());
		} else if (value instanceof StringBuffer) {
			return new StringBuffer(value.toString());
		}
		throw new UnsupportedOperationException(value.getClass() + " copying not supported");
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
	 * @param <T>
	 * 		Value type.
	 *
	 * @return New instance of type.
	 */
	@SuppressWarnings("unchecked")
	public static <T> AbstractSimulatedValue<T> initialize(List<AbstractInsnNode> insns, TypeChecker typeChecker, Type type) {
		return (AbstractSimulatedValue<T>) TYPE_PRODUCERS.get(type.getInternalName()).apply(insns, typeChecker);
	}

	static {
		TYPE_PRODUCERS.put("java/lang/StringBuilder", (insns, typeChecker) ->
				new ReflectionSimulatedValue(insns, Type.getObjectType("java/lang/StringBuilder"), new StringBuilder(), typeChecker));
		TYPE_PRODUCERS.put("java/lang/StringBuffer", (insns, typeChecker) ->
				new ReflectionSimulatedValue(insns, Type.getObjectType("java/lang/StringBuffer"), new StringBuffer(), typeChecker));
		TYPE_PRODUCERS.put("java/lang/String", (insns, typeChecker) -> StringSimulatedValue.of(insns, typeChecker, ""));
	}
}
