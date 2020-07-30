package me.coley.analysis;

import me.coley.analysis.cfg.BlockHandler;
import me.coley.analysis.exception.ResolvableExceptionFactory;
import me.coley.analysis.exception.SimFailedException;
import me.coley.analysis.exception.TypeMismatchKind;
import me.coley.analysis.util.FlowUtil;
import me.coley.analysis.value.AbstractValue;
import me.coley.analysis.value.NullConstantValue;
import me.coley.analysis.value.PrimitiveValue;
import me.coley.analysis.value.ReturnAddressValue;
import me.coley.analysis.value.simulated.AnyValue;
import me.coley.analysis.value.simulated.AbstractSimulatedValue;
import me.coley.analysis.value.UninitializedValue;
import me.coley.analysis.value.VirtualValue;
import me.coley.analysis.value.simulated.StringValue;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.BasicVerifier;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Interpreter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.objectweb.asm.Opcodes.*;
import static me.coley.analysis.util.TypeUtil.*;
import static me.coley.analysis.util.CollectUtils.*;

/**
 * A modified version of ASM's {@link BasicVerifier} to use {@link AbstractValue}.<br>
 * Additionally, a few extra verification steps are taken and simple math and types are calculated.
 *
 * @author Matt
 */
public class SimInterpreter extends Interpreter<AbstractValue> {
	private final Map<AbstractInsnNode, AnalyzerException> badTypeInsns = new HashMap<>();
	private ResolvableExceptionFactory exceptionFactory;
	private StaticInvokeFactory staticInvokeFactory;
	private StaticGetFactory staticGetFactory;
	private ParameterFactory parameterFactory;
	private BlockHandler blockHandler;
	private TypeChecker typeChecker;
	private SimAnalyzer analyzer;

	/**
	 * Create an interpreter.
	 */
	public SimInterpreter() {
		super(Opcodes.ASM8);
	}

	// TODO: Make all of these LoggedAnalyzerException where applicable

	/**
	 * @return Map of instructions to their thrown analyzer errors.
	 */
	public Map<AbstractInsnNode, AnalyzerException> getProblemInsns() {
		return badTypeInsns;
	}

	/**
	 * @param analyzer
	 * 		Analyzer instance that runs the current interpreter.
	 */
	public void setAnalyzer(SimAnalyzer analyzer) {
		this.analyzer = analyzer;
	}

	/**
	 * @return Factory to generate resolvable exceptions.
	 */
	public ResolvableExceptionFactory getExceptionFactory() {
		return exceptionFactory;
	}

	/**
	 * @param exceptionFactory
	 * 		Factory to generate resolvable exceptions.
	 */
	public void setExceptionFactory(ResolvableExceptionFactory exceptionFactory) {
		this.exceptionFactory = exceptionFactory;
	}

	/**
	 * @return Factory to generate values from static method calls.
	 */
	public StaticInvokeFactory getStaticInvokeFactory() {
		return staticInvokeFactory;
	}

	/**
	 * @param staticInvokeFactory
	 * 		Factory to generate values from static method calls.
	 */
	public void setStaticInvokeFactory(StaticInvokeFactory staticInvokeFactory) {
		this.staticInvokeFactory = staticInvokeFactory;
	}

	/**
	 * @return Factory to generate values from static field get calls.
	 */
	public StaticGetFactory getStaticGetFactory() {
		return staticGetFactory;
	}

	/**
	 * @param staticGetFactory
	 * 		Factory to generate values from static field get calls.
	 */
	public void setStaticGetFactory(StaticGetFactory staticGetFactory) {
		this.staticGetFactory = staticGetFactory;
	}

	/**
	 * @returnFactory to generate values for parameters in the initial frame.
	 */
	public ParameterFactory getParameterFactory() {
		return parameterFactory;
	}

	/**
	 * @param parameterFactory
	 * 		Factory to generate values for parameters in the initial frame.
	 */
	public void setParameterFactory(ParameterFactory parameterFactory) {
		this.parameterFactory = parameterFactory;
	}

	/**
	 * @param blockHandler
	 * 		Block handler to determine scope.
	 */
	public void setBlockHandler(BlockHandler blockHandler) {
		this.blockHandler = blockHandler;
	}

	/**
	 * @return Block handler to determine scope.
	 */
	public BlockHandler getBlockHandler() {
		return blockHandler;
	}

	/**
	 * @return Type checker for parent/child relation validation.
	 */
	public TypeChecker getTypeChecker() {
		return typeChecker;
	}

	/**
	 * @param typeChecker
	 * 		Type checker for parent/child relation validation.
	 */
	public void setTypeChecker(TypeChecker typeChecker) {
		this.typeChecker = typeChecker;
	}

	/**
	 * @return {@code true}  when problems have been reported.
	 */
	public boolean hasReportedProblems() {
		return !badTypeInsns.isEmpty();
	}

	private void handleOpqaues(AbstractInsnNode insn, AbstractValue value) {
		if (value.isPrimitive() && value.isValueResolved()) {
			int p1 = ((PrimitiveValue) value).getIntValue();
			boolean gotoDestination = false;
			switch(insn.getOpcode()) {
				case IFEQ:
					gotoDestination = p1 == 0;
					break;
				case IFNE:
					gotoDestination = p1 != 0;
					break;
				case IFLT:
					gotoDestination = p1 < 0;
					break;
				case IFGE:
					gotoDestination = p1 >= 0;
					break;
				case IFGT:
					gotoDestination = p1 > 0;
					break;
				case IFLE:
					gotoDestination = p1 <= 0;
					break;
				default:
					break;
			}
			analyzer.setOpaqueJump(insn, gotoDestination);
		}
	}

	private void handleOpqaues(AbstractInsnNode insn, AbstractValue value1, AbstractValue value2) {
		if (value1.isPrimitive() && value1.isValueResolved() && value2.isPrimitive() && value2.isValueResolved()) {
			int p1 = ((PrimitiveValue) value1).getIntValue();
			int p2 = ((PrimitiveValue) value2).getIntValue();
			boolean gotoDestination = false;
			switch(insn.getOpcode()) {
				case IF_ICMPEQ:
					gotoDestination = p1 == p2;
					break;
				case IF_ICMPNE:
					gotoDestination = p1 != p2;
					break;
				case IF_ICMPLT:
					gotoDestination = p1 < p2;
					break;
				case IF_ICMPGE:
					gotoDestination = p1 >= p2;
					break;
				case IF_ICMPGT:
					gotoDestination = p1 > p2;
					break;
				case IF_ICMPLE:
					gotoDestination = p1 <= p2;
					break;
				default:
					break;
			}
			analyzer.setOpaqueJump(insn, gotoDestination);
		}
	}

	private void markBad(AbstractInsnNode insn, AnalyzerException e) {
		badTypeInsns.put(insn, e);
	}

	private AbstractValue newValueOrVirtualized(AbstractInsnNode insn, Type type) {
		if (AbstractSimulatedValue.supported(type))
			return AbstractSimulatedValue.initialize(Collections.singletonList(insn), typeChecker, type);
		return newValue(insn, type);
	}

	private AbstractValue newValue(AbstractInsnNode insn, Type type) {
		if (type == null)
			return UninitializedValue.UNINITIALIZED_VALUE;
		else if (type == Type.VOID_TYPE)
			return null;
		else if (type.getSort() <= Type.DOUBLE)
			return new PrimitiveValue(insn, type);
		return VirtualValue.ofVirtual(insn, typeChecker, type);
	}

	private AbstractValue newValue(List<AbstractInsnNode> insns, Type type) {
		if (type == null)
			return UninitializedValue.UNINITIALIZED_VALUE;
		else if (type == Type.VOID_TYPE)
			return null;
		else if (type.getSort() <= Type.DOUBLE)
			return new PrimitiveValue(insns, type, null);
		return VirtualValue.ofVirtual(insns, typeChecker, type);
	}

	@Override
	public AbstractValue newValue(Type type) {
		throw new UnsupportedOperationException("Interpreter called default implementation of 'newValue'\n" +
				"Should use more expressive call instead.");
	}

	@Override
	public AbstractValue newReturnTypeValue(Type type) {
		return newValue((List<AbstractInsnNode>) null, type);
	}

	@Override
	public AbstractValue newEmptyValue(int local) {
		return UninitializedValue.UNINITIALIZED_VALUE;
	}

	@Override
	public AbstractValue newParameterValue(boolean isInstanceMethod, int local, Type type) {
		// Supply parameter value if factory instantiated.
		if (parameterFactory != null) {
			AbstractValue value = parameterFactory.createParameterValue(isInstanceMethod, local, type);
			if (value != null)
				return value;
		}
		// Fallback, assume dummy type value
		return newValue((List<AbstractInsnNode>) null, type);
	}

	@Override
	public AbstractValue newExceptionValue(TryCatchBlockNode tryCatch,
										   Frame<AbstractValue> handlerFrame, Type exceptionType) {
		return newValue(tryCatch.handler, exceptionType);
	}

	@Override
	public AbstractValue newOperation(AbstractInsnNode insn) throws AnalyzerException {
		switch (insn.getOpcode()) {
			case ACONST_NULL:
				return NullConstantValue.newNull(insn);
			case ICONST_M1:
				return PrimitiveValue.ofInt(insn, -1);
			case ICONST_0:
				return PrimitiveValue.ofInt(insn, 0);
			case ICONST_1:
				return PrimitiveValue.ofInt(insn, 1);
			case ICONST_2:
				return PrimitiveValue.ofInt(insn, 2);
			case ICONST_3:
				return PrimitiveValue.ofInt(insn, 3);
			case ICONST_4:
				return PrimitiveValue.ofInt(insn, 4);
			case ICONST_5:
				return PrimitiveValue.ofInt(insn, 5);
			case LCONST_0:
				return PrimitiveValue.ofLong(insn, 0L);
			case LCONST_1:
				return PrimitiveValue.ofLong(insn, 1L);
			case FCONST_0:
				return PrimitiveValue.ofFloat(insn, 0.0F);
			case FCONST_1:
				return PrimitiveValue.ofFloat(insn, 1.0F);
			case FCONST_2:
				return PrimitiveValue.ofFloat(insn, 2.0F);
			case DCONST_0:
				return PrimitiveValue.ofDouble(insn, 0.0);
			case DCONST_1:
				return PrimitiveValue.ofDouble(insn, 1.0);
			case BIPUSH:
			case SIPUSH:
				return PrimitiveValue.ofInt(insn, ((IntInsnNode) insn).operand);
			case LDC:
				Object value = ((LdcInsnNode) insn).cst;
				if (value instanceof Integer) {
					return PrimitiveValue.ofInt(insn, (int) value);
				} else if (value instanceof Float) {
					return PrimitiveValue.ofFloat(insn, (float) value);
				} else if (value instanceof Long) {
					return PrimitiveValue.ofLong(insn, (long) value);
				} else if (value instanceof Double) {
					return PrimitiveValue.ofDouble(insn, (double) value);
				} else if (value instanceof String) {
					return StringValue.of(insn, typeChecker, (String) value);
				} else if (value instanceof Type) {
					Type type =  (Type) value;
					int sort = type.getSort();
					if (sort == Type.OBJECT || sort == Type.ARRAY) {
						return VirtualValue.ofClass(insn, typeChecker, type);
					} else if (sort == Type.METHOD) {
						return newValue(insn, Type.getObjectType("java/lang/invoke/MethodType"));
					} else {
						throw new AnalyzerException(insn, "Illegal LDC value " + value);
					}
				} else if (value instanceof Handle) {
					return newValue(insn, Type.getObjectType("java/lang/invoke/MethodHandle"));
				} else if (value instanceof ConstantDynamic) {
					return newValue(insn, Type.getType(((ConstantDynamic) value).getDescriptor()));
				} else {
					throw new AnalyzerException(insn, "Illegal LDC value " + value);
				}
			case JSR:
				return ReturnAddressValue.newRet(insn);
			case GETSTATIC:
				FieldInsnNode fin = (FieldInsnNode) insn;
				Type type = Type.getType(fin.desc);
				if (staticGetFactory != null) {
					return staticGetFactory.getStatic(fin);
				}
				return newValue(insn, type);
			case NEW:
				return newValueOrVirtualized(insn, Type.getObjectType(((TypeInsnNode) insn).desc));
			default:
				throw new IllegalStateException();
		}
	}

	@Override
	public AbstractValue copyOperation(AbstractInsnNode insn, AbstractValue value) throws AnalyzerException {
		// Fetch type from instruction
		Type insnType = null;
		boolean load = false;
		switch(insn.getOpcode()) {
			case ILOAD:
				load = true;
			case ISTORE:
				insnType = Type.INT_TYPE;
				break;
			case LLOAD:
				load = true;
			case LSTORE:
				insnType = Type.LONG_TYPE;
				break;
			case FLOAD:
				load = true;
			case FSTORE:
				insnType = Type.FLOAT_TYPE;
				break;
			case DLOAD:
				load = true;
			case DSTORE:
				insnType = Type.DOUBLE_TYPE;
				break;
			case ALOAD:
				load = true;
				if (value != UninitializedValue.UNINITIALIZED_VALUE && !value.isReference())
					throw new AnalyzerException(insn, "Expected a reference type.");
				insnType = value.getType();
				break;
			case ASTORE:
				if (!value.isReference() && !(value instanceof ReturnAddressValue))
					throw new AnalyzerException(insn, "Expected a reference or return-address type.");
				insnType = value.getType();
				break;
			default:
				// DUP, DUP_X1, DUP_X2, DUP2, DUP2_X1, DUP2_X2, SWAP
				break;
		}
		// Very simple type verification, don't try to mix primitives and non-primitives
		Type argType = value.getType();
		if(insnType != null && argType != null) {
			if(insnType.getSort() == Type.OBJECT && isPrimitive(argType))
				throw new AnalyzerException(insn, "Cannot mix primitive value with type-variable instruction");
			else if(argType.getSort() == Type.OBJECT && isPrimitive(insnType))
				throw new AnalyzerException(insn, "Cannot mix type value with primitive-variable instruction");
		}
		// If we're operating on a load-instruction we want the return value to
		// relate to the type of the instruction.
		if(load && insnType != value.getType())
			return newValue(add(value.getInsns(), insn), insnType);
		// Types match or type is null (so either a store operation)
		return value.copy(insn);
	}

	@Override
	public AbstractValue unaryOperation(AbstractInsnNode insn, AbstractValue value) throws AnalyzerException {
		switch(insn.getOpcode()) {
			case INEG:
				if (isValueUnknown(value))
					return newValue(add(value.getInsns(), insn), Type.INT_TYPE);
				return PrimitiveValue.ofInt(add(value.getInsns(), insn), -toInt(value));
			case IINC:
				return PrimitiveValue.ofInt(add(value.getInsns(), insn), ((IincInsnNode) insn).incr);
			case L2I:
			case F2I:
			case D2I:
			case I2B:
			case I2C:
			case I2S:
				if (isValueUnknown(value))
					return newValue(add(value.getInsns(), insn), Type.INT_TYPE);
				return PrimitiveValue.ofInt(add(value.getInsns(), insn), toInt(value));
			case FNEG:
				if (isValueUnknown(value))
					return newValue(add(value.getInsns(), insn), Type.FLOAT_TYPE);
				return PrimitiveValue.ofFloat(add(value.getInsns(), insn), -toFloat(value));
			case I2F:
			case L2F:
			case D2F:
				if (isValueUnknown(value))
					return newValue(add(value.getInsns(), insn), Type.FLOAT_TYPE);
				return PrimitiveValue.ofFloat(add(value.getInsns(), insn), toFloat(value));
			case LNEG:
				if (isValueUnknown(value))
					return newValue(add(value.getInsns(), insn), Type.LONG_TYPE);
				return PrimitiveValue.ofLong(add(value.getInsns(), insn), -toLong(value));
			case I2L:
			case F2L:
			case D2L:
				if (isValueUnknown(value))
					return newValue(add(value.getInsns(), insn), Type.LONG_TYPE);
				return PrimitiveValue.ofLong(add(value.getInsns(), insn), toLong(value));
			case DNEG:
				if (isValueUnknown(value))
					return newValue(add(value.getInsns(), insn), Type.DOUBLE_TYPE);
				return PrimitiveValue.ofDouble(add(value.getInsns(), insn), -toDouble(value));
			case I2D:
			case L2D:
			case F2D:
				if (isValueUnknown(value))
					return newValue(add(value.getInsns(), insn), Type.DOUBLE_TYPE);
				return PrimitiveValue.ofDouble(add(value.getInsns(), insn), toDouble(value));
			case IFEQ:
			case IFNE:
			case IFLT:
			case IFGE:
			case IFGT:
			case IFLE:
				handleOpqaues(insn, value);
			case TABLESWITCH:
			case LOOKUPSWITCH:
				if (!(isSubTypeOf(typeChecker, value.getType(), Type.INT_TYPE)
						|| isSubTypeOf(typeChecker, value.getType(), Type.BOOLEAN_TYPE)))
					throw new AnalyzerException(insn, "Expected int type.");
				return null;
			case IRETURN:
				if (!(isSubTypeOf(typeChecker, value.getType(), Type.INT_TYPE)
						|| isSubTypeOf(typeChecker, value.getType(), Type.BOOLEAN_TYPE)))
					throw new AnalyzerException(insn, "Expected int return type.");
				return null;
			case LRETURN:
				if (!isSubTypeOf(typeChecker, value.getType(), Type.LONG_TYPE))
					throw new AnalyzerException(insn, "Expected long return type.");
				return null;
			case FRETURN:
				if (!isSubTypeOf(typeChecker, value.getType(), Type.FLOAT_TYPE))
					throw new AnalyzerException(insn, "Expected float return type.");
				return null;
			case DRETURN:
				if (!isSubTypeOf(typeChecker, value.getType(), Type.DOUBLE_TYPE))
					throw new AnalyzerException(insn, "Expected double return type.");
				return null;
			case ARETURN:
				if (!value.isReference())
					throw new AnalyzerException(insn, "Expected reference return type");
				return null;
			case PUTSTATIC: {
				// Value == item on stack
				FieldInsnNode fin = (FieldInsnNode) insn;
				Type fieldType = Type.getType(fin.desc);
				if (!isSubTypeOf(typeChecker, value.getType(), fieldType))
					markBad(insn, exceptionFactory.unexpectedType(fieldType, value.getType(), insn, value, TypeMismatchKind.PUTSTATIC));
				return null;
			}
			case GETFIELD: {
				// Value == field owner instance
				// - Check instance context is of the owner class
				FieldInsnNode fin = (FieldInsnNode) insn;
				Type ownerType = Type.getObjectType(fin.owner);
				if (!isSubTypeOf(typeChecker, value.getType(), ownerType))
					markBad(insn, exceptionFactory.unexpectedType(Type.getObjectType(fin.owner),
							value.getType(), insn, value, TypeMismatchKind.GETFIELD));
				Type type = Type.getType(fin.desc);
				return newValue(add(value.getInsns(), insn), type);
			}
			case NEWARRAY:
				switch(((IntInsnNode) insn).operand) {
					case T_BOOLEAN:
						return newValue(add(value.getInsns(), insn), Type.getType("[Z"));
					case T_CHAR:
						return newValue(add(value.getInsns(), insn), Type.getType("[C"));
					case T_BYTE:
						return newValue(add(value.getInsns(), insn), Type.getType("[B"));
					case T_SHORT:
						return newValue(add(value.getInsns(), insn), Type.getType("[S"));
					case T_INT:
						return newValue(add(value.getInsns(), insn), Type.getType("[I"));
					case T_FLOAT:
						return newValue(add(value.getInsns(), insn), Type.getType("[F"));
					case T_DOUBLE:
						return newValue(add(value.getInsns(), insn), Type.getType("[D"));
					case T_LONG:
						return newValue(add(value.getInsns(), insn), Type.getType("[J"));
					default:
						break;
				}
				throw new AnalyzerException(insn, "Invalid array type specified in instruction");
			case ANEWARRAY:
				return newValue(add(value.getInsns(), insn), Type.getType("[" + Type.getObjectType(((TypeInsnNode) insn).desc)));
			case ARRAYLENGTH:
				if (value.getValue() instanceof Unresolved && !((Unresolved) value.getValue()).isArray())
					markBad(insn, new AnalyzerException(insn, "Expected an array type."));
				return newValue(add(value.getInsns(), insn), Type.INT_TYPE);
			case ATHROW:
				if (!value.isReference())
					throw new AnalyzerException(insn, "Expected reference type on stack for ATHROW.");
				return null;
			case CHECKCAST:
				if (!value.isReference())
					throw new AnalyzerException(insn, "Expected reference type on stack for CHECKCAST.");
				return newValue(add(value.getInsns(), insn), Type.getObjectType(((TypeInsnNode) insn).desc));
			case INSTANCEOF:
				return newValue(add(value.getInsns(), insn), Type.INT_TYPE);
			case MONITORENTER:
			case MONITOREXIT:
				if (!value.isReference())
					throw new AnalyzerException(insn, "Expected a reference type for monitor.");
				return null;
			case IFNULL:
			case IFNONNULL:
				if (!value.isReference())
					throw new AnalyzerException(insn, "Expected a reference type ifnull/nonnull.");
				value.setNullCheckedBy((JumpInsnNode) insn);
				return null;
			default:
				throw new IllegalStateException();
		}
	}

	@Override
	public AbstractValue binaryOperation(AbstractInsnNode insn, AbstractValue value1, AbstractValue value2)  {
		// Modified from BasicVerifier
		Type expected1;
		Type expected2;
		boolean wasAALOAD = false;
		switch (insn.getOpcode()) {
			case IALOAD:
				expected1 = Type.getType("[I");
				expected2 = Type.INT_TYPE;
				break;
			case BALOAD:
				if (isSubTypeOf(typeChecker, value1.getType(), Type.getType("[Z"))) {
					expected1 = Type.getType("[Z");
				} else {
					expected1 = Type.getType("[B");
				}
				expected2 = Type.INT_TYPE;
				break;
			case CALOAD:
				expected1 = Type.getType("[C");
				expected2 = Type.INT_TYPE;
				break;
			case SALOAD:
				expected1 = Type.getType("[S");
				expected2 = Type.INT_TYPE;
				break;
			case LALOAD:
				expected1 = Type.getType("[J");
				expected2 = Type.INT_TYPE;
				break;
			case FALOAD:
				expected1 = Type.getType("[F");
				expected2 = Type.INT_TYPE;
				break;
			case DALOAD:
				expected1 = Type.getType("[D");
				expected2 = Type.INT_TYPE;
				break;
			case AALOAD:
				expected1 = Type.getType("[Ljava/lang/Object;");
				expected2 = Type.INT_TYPE;
				wasAALOAD = true;
				break;
			case IF_ICMPEQ:
			case IF_ICMPNE:
			case IF_ICMPLT:
			case IF_ICMPGE:
			case IF_ICMPGT:
			case IF_ICMPLE:
				handleOpqaues(insn, value1, value2);
			case IADD:
			case ISUB:
			case IMUL:
			case IDIV:
			case IREM:
			case ISHL:
			case ISHR:
			case IUSHR:
			case IAND:
			case IOR:
			case IXOR:
				expected1 = Type.INT_TYPE;
				expected2 = Type.INT_TYPE;
				break;
			case FADD:
			case FSUB:
			case FMUL:
			case FDIV:
			case FREM:
			case FCMPL:
			case FCMPG:
				expected1 = Type.FLOAT_TYPE;
				expected2 = Type.FLOAT_TYPE;
				break;
			case LADD:
			case LSUB:
			case LMUL:
			case LDIV:
			case LREM:
			case LAND:
			case LOR:
			case LXOR:
			case LCMP:
				expected1 = Type.LONG_TYPE;
				expected2 = Type.LONG_TYPE;
				break;
			case LSHL:
			case LSHR:
			case LUSHR:
				expected1 = Type.LONG_TYPE;
				expected2 = Type.INT_TYPE;
				break;
			case DADD:
			case DSUB:
			case DMUL:
			case DDIV:
			case DREM:
			case DCMPL:
			case DCMPG:
				expected1 = Type.DOUBLE_TYPE;
				expected2 = Type.DOUBLE_TYPE;
				break;
			case IF_ACMPEQ:
			case IF_ACMPNE:
				expected1 = OBJECT_TYPE;
				expected2 = OBJECT_TYPE;
				break;
			case PUTFIELD:
				FieldInsnNode fieldInsn = (FieldInsnNode) insn;
				expected1 = Type.getObjectType(fieldInsn.owner);
				expected2 = Type.getType(fieldInsn.desc);
				break;
			default:
				throw new IllegalStateException();
		}
		if (wasAALOAD && value1 != UninitializedValue.UNINITIALIZED_VALUE &&
				value1.isArray() && value1.getType().getDimensions() > 1) {
			// If we are using AALOAD to load an object reference from an array, we check to see if the
			// reference loaded is the another array (consider int[][], fetching int[]) ...
			// In the bytecode, we don't have any immediate way to validate against an expected type.
			// So we shall do nothing :)
		} else if (value1 != UninitializedValue.UNINITIALIZED_VALUE && value2 != UninitializedValue.UNINITIALIZED_VALUE) {
			if (!isSubTypeOfOrNull(typeChecker, value1, expected1))
				markBad(insn, new AnalyzerException(insn, "First argument not of expected type", expected1, value1));
			else if (!isSubTypeOfOrNull(typeChecker, value2, expected2))
				markBad(insn, new AnalyzerException(insn, "Second argument not of expected type", expected2, value2));
		} else {
			markBad(insn, new AnalyzerException(insn, "Cannot act on uninitialized values", expected2, value2));
		}
		// Update values for non-primitives
		switch(insn.getOpcode()) {
			case FALOAD:
				return newValue(combineAdd(value1.getInsns(), value2.getInsns(), insn), Type.FLOAT_TYPE);
			case LALOAD:
				return newValue(combineAdd(value1.getInsns(), value2.getInsns(), insn), Type.LONG_TYPE);
			case DALOAD:
				return newValue(combineAdd(value1.getInsns(), value2.getInsns(), insn), Type.DOUBLE_TYPE);
			case AALOAD:
				if (value1.getType() == null)
					return newValue(combineAdd(value1.getInsns(), value2.getInsns(), insn), OBJECT_TYPE);
				else
					return newValue(combineAdd(value1.getInsns(), value2.getInsns(), insn),
							Type.getType(value1.getType().getDescriptor().substring(1)));
			case IALOAD:
			case BALOAD:
			case CALOAD:
			case SALOAD:
				return newValue(combineAdd(value1.getInsns(), value2.getInsns(), insn), Type.INT_TYPE);
			case IF_ICMPEQ:
			case IF_ICMPNE:
			case IF_ICMPLT:
			case IF_ICMPGE:
			case IF_ICMPGT:
			case IF_ICMPLE:
			case IF_ACMPEQ:
			case IF_ACMPNE:
			case PUTFIELD:
				return null;
			default:
				break;
		}
		// Update values for primitive operations
		PrimitiveValue p1 = (PrimitiveValue) value1;
		PrimitiveValue p2 = (PrimitiveValue) value2;
		switch(insn.getOpcode()) {
			case IADD:
			case FADD:
			case LADD:
			case DADD:
				return p1.add(insn, p2);
			case ISUB:
			case FSUB:
			case LSUB:
			case DSUB:
				return p1.sub(insn, p2);
			case IMUL:
			case FMUL:
			case LMUL:
			case DMUL:
				return p1.mul(insn, p2);
			case IDIV:
			case FDIV:
			case LDIV:
			case DDIV:
				return p1.div(insn, p2);
			case IREM:
			case FREM:
			case LREM:
			case DREM:
				return p1.rem(insn, p2);
			case ISHL:
			case LSHL:
				return p1.shl(insn, p2);
			case ISHR:
			case LSHR:
				return p1.shr(insn, p2);
			case IUSHR:
			case LUSHR:
				return p1.ushr(insn, p2);
			case IAND:
			case LAND:
				return p1.and(insn, p2);
			case IOR:
			case LOR:
				return p1.or(insn, p2);
			case IXOR:
			case LXOR:
				return p1.xor(insn, p2);
			case LCMP:
			case FCMPL:
			case FCMPG:
			case DCMPL:
			case DCMPG:
				if (p1.getValue() == null || p2.getValue() == null ||
						isValueUnknown(p1) || isValueUnknown(p2))
					return newValue(combineAdd(value1.getInsns(), value2.getInsns(), insn), Type.INT_TYPE);
				double v1 = ((Number) value1.getValue()).doubleValue();
				double v2 = ((Number) value1.getValue()).doubleValue();
				if(v1 > v2)
					return PrimitiveValue.ofInt(combineAdd(value1.getInsns(), value2.getInsns(), insn), 1);
				else if(v1 < v2)
					return PrimitiveValue.ofInt(combineAdd(value1.getInsns(), value2.getInsns(), insn), -1);
				else
					return PrimitiveValue.ofInt(combineAdd(value1.getInsns(), value2.getInsns(), insn), 0);
			default:
				break;
		}
		throw new IllegalStateException();
	}

	@Override
	public AbstractValue ternaryOperation(AbstractInsnNode insn, AbstractValue value1, AbstractValue value2,
										  AbstractValue value3) {
		Type expected1;
		Type expected3;
		switch(insn.getOpcode()) {
			case IASTORE:
				expected1 = Type.getType("[I");
				expected3 = Type.INT_TYPE;
				break;
			case BASTORE:
				if(isSubTypeOf(typeChecker, value1.getType(), Type.getType("[Z"))) {
					expected1 = Type.getType("[Z");
				} else {
					expected1 = Type.getType("[B");
				}
				expected3 = Type.INT_TYPE;
				break;
			case CASTORE:
				expected1 = Type.getType("[C");
				expected3 = Type.INT_TYPE;
				break;
			case SASTORE:
				expected1 = Type.getType("[S");
				expected3 = Type.INT_TYPE;
				break;
			case LASTORE:
				expected1 = Type.getType("[J");
				expected3 = Type.LONG_TYPE;
				break;
			case FASTORE:
				expected1 = Type.getType("[F");
				expected3 = Type.FLOAT_TYPE;
				break;
			case DASTORE:
				expected1 = Type.getType("[D");
				expected3 = Type.DOUBLE_TYPE;
				break;
			case AASTORE:
				expected1 = value1.getType();
				expected3 = OBJECT_TYPE;
				break;
			default:
				throw new AssertionError();
		}
		if(!isSubTypeOf(typeChecker, value1.getType(), expected1))
			markBad(insn, new AnalyzerException(insn, "First argument not of expected type", expected1, value1));
		else if(!Type.INT_TYPE.equals(value2.getType()))
			markBad(insn, new AnalyzerException(insn, "Second argument not an integer", BasicValue.INT_VALUE, value2));
		else if(!isSubTypeOf(typeChecker, value3.getType(), expected3))
			markBad(insn, new AnalyzerException(insn, "Second argument not of expected type", expected3, value3));
		return null;
	}

	@Override
	public AbstractValue naryOperation(AbstractInsnNode insn, List<? extends AbstractValue> values) throws AnalyzerException {
		int opcode = insn.getOpcode();
		if (opcode == MULTIANEWARRAY) {
			// Multi-dimensional array args must all be numeric
			for (AbstractValue value : values)
				if (!Type.INT_TYPE.equals(value.getType()))
					throw new AnalyzerException(insn, "MULTIANEWARRAY argument was not numeric!",
							newValue(insn, Type.INT_TYPE), value);
			return newValue(add(values.stream()
						.flatMap(value -> value.getInsns().stream())
						.collect(Collectors.toList()), insn),
					Type.getType(((MultiANewArrayInsnNode) insn).desc));
		}
		// Handle method invokes
		String methodDescriptor = (opcode == INVOKEDYNAMIC) ?
				((InvokeDynamicInsnNode) insn).desc :
				((MethodInsnNode) insn).desc;
		Type[] args = Type.getArgumentTypes(methodDescriptor);
		// From BasicVerifier
		int i = 0;
		int j = 0;
		if(opcode != INVOKESTATIC && opcode != INVOKEDYNAMIC) {
			MethodInsnNode min = ((MethodInsnNode) insn);
			Type owner = Type.getObjectType(min.owner);
			AbstractValue actual = values.get(i++);
			if(!isSubTypeOf(typeChecker, actual.getType(), owner) &&
					!(isMethodAddSuppressed(min) && actual instanceof NullConstantValue))
				markBad(insn, exceptionFactory.unexpectedMethodHostType(owner, actual.getType(),
						(MethodInsnNode) insn, actual, values, TypeMismatchKind.INVOKE_HOST_TYPE));
		}
		while(i < values.size()) {
			Type expected = args[j++];
			AbstractValue actual = values.get(i++);
			if(!isSubTypeOfOrNull(typeChecker, actual, expected)) {
				markBad(insn, exceptionFactory.unexpectedMethodArgType(expected, actual.getType(),
						insn, actual, values, j - 1, TypeMismatchKind.INVOKE_ARG_TYPE));
			}
		}
		// Get value
		if (opcode == INVOKEDYNAMIC) {
			Type retType = Type.getReturnType(((InvokeDynamicInsnNode) insn).desc);
			return newValue(insn, retType);
		} else if (opcode == INVOKESTATIC) {
			// Attempt to create simulated value
			MethodInsnNode min = (MethodInsnNode) insn;
			try {
				AbstractValue value = AnyValue.ofStaticInvoke(staticInvokeFactory, min, values, typeChecker);
				if (value != null)
					return value;
			} catch(SimFailedException ex) {
				// Do nothing for simulation failing, this is expected in MOST cases.
			}
			// Fallback to virtual value
			Type retType = Type.getReturnType(((MethodInsnNode) insn).desc);
			return newValue(add(values.stream()
						.flatMap(value -> value.getInsns().stream())
						.collect(Collectors.toList()), insn), retType);
		}
		// INVOKEVIRTUAL, INVOKESPECIAL, INVOKEINTERFACE
		MethodInsnNode min = (MethodInsnNode) insn;
		AbstractValue ownerValue = values.get(0);
		if (ownerValue == UninitializedValue.UNINITIALIZED_VALUE) {
			// Instruction acting on an uninitialized variable/value
			throw new AnalyzerException(insn, "Cannot call method on uninitialized reference");
		} else if (ownerValue instanceof NullConstantValue && !isMethodAddSuppressed(min) &&
				!FlowUtil.isNullChecked(getBlockHandler(), ownerValue, insn)) {
			// Instruction acting on a null value, which is illegal
			markBad(insn, exceptionFactory.unexpectedNullReference(
					min, ownerValue, values, TypeMismatchKind.INVOKE_HOST_NULL));
			return newValue(insn, Type.getMethodType(min.desc).getReturnType());
		} else if (ownerValue instanceof NullConstantValue && isMethodAddSuppressed(min)) {
			// Don't you just LOVE edge cases?
			return null;
		} else {
			// Get return value
			if (ownerValue instanceof AbstractSimulatedValue) {
				AbstractSimulatedValue<?> simObject = (AbstractSimulatedValue<?>) ownerValue;
				List<? extends AbstractValue> arguments = values.subList(1, values.size());
				try {
					return simObject.ofVirtualInvoke(min, arguments);
				} catch(SimFailedException ex) {
					// Do nothing for simulation failing, this is expected in MOST cases.
					// This will fallback on VirtualValue behavior
				}
			}
			if (ownerValue instanceof VirtualValue) {
				VirtualValue virtualOwner = (VirtualValue) ownerValue;
				return virtualOwner.ofMethodRef(insn, typeChecker, Type.getMethodType(((MethodInsnNode) insn).desc));
			}
			// Check if we have a null value that has been null checked
			if (ownerValue instanceof NullConstantValue && FlowUtil.isNullChecked(getBlockHandler(), ownerValue, insn))
				return newValue(insn, Type.getMethodType(min.desc).getReturnType());
			throw new AnalyzerException(insn, "Virtual method context could not be resolved");
		}
	}

	@Override
	public void returnOperation(AbstractInsnNode insn, AbstractValue value, AbstractValue expected) {
		if(!isSubTypeOfOrNull(typeChecker, value, expected))
			markBad(insn, exceptionFactory.unexpectedType(expected.getType(), value.getType(), insn, value, TypeMismatchKind.RETURN));
	}

	@Override
	public AbstractValue merge(AbstractValue value1, AbstractValue value2) {
		// Handle uninitialized
		//  - and NO... Do not make another case checking against value1
		//  - Trust me, just dont.
		if (value2 == UninitializedValue.UNINITIALIZED_VALUE)
			return value1;
		// Handle equality
		if (value1.equals(value2))
			return value1;
		// Handle null
		//  - NULL can be ANY type, so... it wins the "common super type" here
		List<AbstractInsnNode> merged =
				distinct(combine(value1.getInsns(), value2.getInsns()));
		if (value2 instanceof NullConstantValue)
			return value1.isNull() ? AbstractValue.ofDefault(null, typeChecker, value1.getType()) : newValue(merged, value1.getType());
		else if (value1 instanceof NullConstantValue)
			return value2.isNull() ? AbstractValue.ofDefault(null, typeChecker, value2.getType()) : newValue(merged, value2.getType());
		// Check standard merge
		if (value1.canMerge(value2))
			return newValue(merged, value1.getType());
		else if (value2.canMerge(value1))
			return newValue(merged, value2.getType());
		return UninitializedValue.UNINITIALIZED_VALUE;
	}

	// ============================ PRIVATE UTILITIES  ============================ //

	private boolean isValueUnknown(AbstractValue value) {
		return value.getValue() == null || value.getValue() instanceof Unresolved;
	}

	private float toFloat(AbstractValue value) {
		return ((Number) value.getValue()).floatValue();
	}

	private double toDouble(AbstractValue value) {
		return ((Number) value.getValue()).doubleValue();
	}

	private int toInt(AbstractValue value) {
		return ((Number) value.getValue()).intValue();
	}

	private long toLong(AbstractValue value) {
		return ((Number) value.getValue()).longValue();
	}

	private static boolean isMethodAddSuppressed(MethodInsnNode insn) {
		// Seriously, wtf is this?
		// Compile the code below:
		//
		//// try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {}
		//// finally {  is.close();  }
		//
		// This will literally generate a call that behaves like "null.addSuppressed(Throwable)"
		// - It generates a method call on a variable that is ALWAYS null
		//
		// And that is why we have this check...
		return insn.owner.equals("java/lang/Throwable") &&
				insn.name.equals("addSuppressed") &&
				insn.desc.equals("(Ljava/lang/Throwable;)V");
	}
}
