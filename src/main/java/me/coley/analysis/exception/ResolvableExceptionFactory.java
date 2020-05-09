package me.coley.analysis.exception;

import me.coley.analysis.util.InsnUtil;
import me.coley.analysis.util.TypeUtil;
import me.coley.analysis.value.AbstractValue;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;

import java.util.List;

import static org.objectweb.asm.Opcodes.INVOKEDYNAMIC;

/**
 * Factory for generating {@link ResolvableAnalyzerException}.
 *
 * @author Matt
 */
public class ResolvableExceptionFactory {

	/**
	 * @param expectedType
	 * 		Expected type.
	 * @param actualType
	 * 		Actual type that was given.
	 * @param insn
	 * 		Instruction that exception occurred on.
	 * @param actualValue
	 * 		Value that was given, host of type.
	 * @param errorType
	 * 		Error context.
	 *
	 * @return An analyzer error that allows post-analysis resolving of the problem.
	 */
	public ResolvableAnalyzerException unexpectedType(
			Type expectedType,
			Type actualType,
			AbstractInsnNode insn,
			AbstractValue actualValue,
			TypeMismatchKind errorType) {
		switch(errorType) {
			case PUTSTATIC:
				return new ResolvableAnalyzerException((methodNode, frames) -> {
					// Validate that the argument value is no longer null when stack-frames are filled out
					Frame<AbstractValue> frame = frames[InsnUtil.index(insn)];
					AbstractValue methodContext = frame.getStack(frame.getStackSize() - 1);
					return TypeUtil.isSubTypeOfOrNull(methodContext, expectedType);
				}, insn, "Expected type: " + expectedType);
			case GETFIELD:
				return new ResolvableAnalyzerException((methodNode, frames) -> {
					// Validate that the top of the stack matches the expected type
					Frame<AbstractValue> frame = frames[InsnUtil.index(insn)];
					AbstractValue fieldContext = frame.getStack(frame.getStackSize() - 1);
					return TypeUtil.isSubTypeOf(fieldContext.getType(), expectedType);
				}, insn, "Expected type: " + expectedType);
			case RETURN:
				return new ResolvableAnalyzerException((methodNode, frames) -> {
					// Validate that the top of the stack matches the expected type
					Frame<AbstractValue> frame = frames[InsnUtil.index(insn)];
					AbstractValue returnValue = frame.getStack(frame.getStackSize() - 1);
					return TypeUtil.isSubTypeOfOrNull(returnValue, expectedType);
				}, insn, "Incompatible return type, found '" + actualType + "', expected: " +
						expectedType, expectedType, actualValue);
			default:
				break;
		}
		throw new IllegalStateException("Unhandled exception in factory");
	}

	/**
	 * @param expectedType
	 * 		Expected type.
	 * @param actualType
	 * 		Actual type that was given.
	 * @param insn
	 * 		Instruction that exception occurred on.
	 * @param actualValue
	 * 		Value that was given, host of type.
	 * @param stackValues
	 * 		Values that were on the stack.
	 * @param errorType
	 * 		Error context.
	 *
	 * @return An analyzer error that allows post-analysis resolving of the problem.
	 */
	public AnalyzerException unexpectedMethodHostType(
			Type expectedType,
			Type actualType,
			MethodInsnNode insn,
			AbstractValue actualValue,
			List<? extends AbstractValue> stackValues,
			TypeMismatchKind errorType) {
		Type[] args = Type.getArgumentTypes(insn.desc);
		Type owner = Type.getObjectType(insn.owner);
		return new ResolvableAnalyzerException((methodNode, frames) -> {
			// Validate that the owner value is no longer null when stack-frames are filled out
			Frame<AbstractValue> frame = frames[InsnUtil.index(insn)];
			// TODO: Validate the stack index is correct here
			AbstractValue methodContext =
					frame.getStack(frame.getStackSize() - (args.length + 1));
			return TypeUtil.isSubTypeOf(methodContext.getType(), owner);
		}, insn, "Method owner does not match type on stack");
	}

	/**
	 * @param expectedType
	 * 		Expected type.
	 * @param actualType
	 * 		Actual type that was given.
	 * @param insn
	 * 		Instruction that exception occurred on.
	 * @param actualValue
	 * 		Value that was given, host of type.
	 * @param stackValues
	 * 		Values that were on the stack.
	 * @param argIndex
	 * 		Method argument index that caused the type mismatch.
	 * @param errorType
	 * 		Error context.
	 *
	 * @return An analyzer error that allows post-analysis resolving of the problem.
	 */
	public AnalyzerException unexpectedMethodArgType(
			Type expectedType, Type actualType,
			AbstractInsnNode insn,
			AbstractValue actualValue,
			List<? extends AbstractValue> stackValues,
			int argIndex,
			TypeMismatchKind errorType) {
		String methodDescriptor = (insn.getOpcode() == INVOKEDYNAMIC) ?
				((InvokeDynamicInsnNode) insn).desc :
				((MethodInsnNode) insn).desc;
		Type[] args = Type.getArgumentTypes(methodDescriptor);
		return new ResolvableAnalyzerException((methodNode, frames) -> {
			// Validate that the argument value is no longer null when stack-frames are filled out
			Frame<AbstractValue> frame = frames[InsnUtil.index(insn)];
			AbstractValue methodContext = frame.getStack(frame.getStackSize() - (args.length - argIndex + 1));
			return TypeUtil.isSubTypeOfOrNull(methodContext, expectedType);
		},insn, "Argument type was \"" + actualType + "\" but expected \"" + expectedType + "\"");
	}

	/**
	 * @param insn
	 * 		Instruction that exception occurred on.
	 * @param actualValue
	 * 		Value that was given.
	 * @param stackValues
	 * 		Values that were on the stack.
	 * @param errorType
	 * 		Error context.
	 *
	 * @return An analyzer error that allows post-analysis resolving of the problem.
	 */
	public AnalyzerException unexpectedNullReference(
			MethodInsnNode insn,
			AbstractValue actualValue,
			List<? extends AbstractValue> stackValues,
			TypeMismatchKind errorType) {
		Type[] args = Type.getArgumentTypes(insn.desc);
		return new ResolvableAnalyzerException((method, frames) -> {
			// Validate that the owner value is no longer null when stack-frames are filled out
			Frame<AbstractValue> frame = frames[InsnUtil.index(insn)];
			AbstractValue methodContext = frame.getStack(frame.getStackSize() - (args.length + 1));
			return !methodContext.isNull();
		}, insn, "Cannot call method on null reference");
	}
}
