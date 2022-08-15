package me.coley.analysis.util;

import me.coley.analysis.value.AbstractValue;
import org.objectweb.asm.tree.analysis.Frame;

/**
 * Frame utilities.
 *
 * @author Matt Coley
 */
public class FrameUtil {

	/**
	 * Fetch the top stack in the frame.
	 *
	 * @param frame
	 * 		Frame to look at.
	 *
	 * @return Value at top of stack.
	 */
	public static AbstractValue getTopStack(Frame<AbstractValue> frame) {
		return frame.getStack(frame.getStackSize() - 1);
	}

	/**
	 * Fetch the top stack value in the frame.
	 *
	 * @param frame
	 * 		Frame to look at.
	 * @param <T>
	 * 		Type to return.
	 *
	 * @return Literal value at top of stack.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getTopStackLiteral(Frame<AbstractValue> frame) {
		return (T) getTopStack(frame).getValue();
	}

	/**
	 * Fetch the value off of the stack with the given offset from the top slot in the frame.
	 *
	 * @param frame
	 * 		Frame to look at.
	 * @param offset
	 * 		Offset from top to fetch.
	 *
	 * @return Value at offset from the top of the stack.
	 */
	public static AbstractValue getStackFromTop(Frame<AbstractValue> frame, int offset) {
		return frame.getStack(frame.getStackSize() - (1 + offset));
	}

	/**
	 * Fetch the literal value off of the stack with the given offset from the top slot in the frame.
	 *
	 * @param frame
	 * 		Frame to look at.
	 * @param offset
	 * 		Offset from top to fetch.
	 * @param <T>
	 * 		Type to return.
	 *
	 * @return Literal value at offset from the top of the stack.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getStackLiteralFromTop(Frame<AbstractValue> frame, int offset) {
		return (T) getStackFromTop(frame, offset).getValue();
	}

	/**
	 * Fetch the top {@code N} items of the stack, where {@code N = argCount}.
	 *
	 * @param frame
	 * 		Frame to look at.
	 * @param argCount
	 * 		Number of items off of the stack to pull.
	 *
	 * @return Array of literal values from the stack.
	 */
	public static Object[] getStackArgumentLiterals(Frame<AbstractValue> frame, int argCount) {
		Object[] args = new Object[argCount];
		for (int i = 0; i < argCount; i++)
			args[i] = getStackLiteralFromTop(frame, (argCount - i) - 1);
		return args;
	}
}
