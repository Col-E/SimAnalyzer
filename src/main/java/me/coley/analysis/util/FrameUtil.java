package me.coley.analysis.util;

import me.coley.analysis.SimAnalyzer;
import me.coley.analysis.SimInterpreter;
import me.coley.analysis.value.AbstractValue;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;

/**
 * Frame utilities.
 *
 * @author Matt
 */
public class FrameUtil {
	/**
	 * Analyze and retch the frames of the given method, if it is valid.
	 *
	 * @param owner
	 * 		Name of method's defining class.
	 * @param method
	 * 		Method instance.
	 *
	 * @return Analyzed frames of the method.
	 *
	 * @throws AnalyzerException
	 * 		When analysis fails.
	 */
	public static Frame<AbstractValue>[] getFrames(String owner, MethodNode method) throws AnalyzerException {
		return new SimAnalyzer(new SimInterpreter()).analyze(owner, method);
	}

	/**
	 * Fetch the top stack value in the frame.
	 *
	 * @param frame
	 * 		Frame to look at.
	 * @param <T>
	 * 		Type to return.
	 *
	 * @return Value at top of stack.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getTopStack(Frame<AbstractValue> frame) {
		return (T) frame.getStack(frame.getStackSize() - 1).getValue();
	}

	/**
	 * Fetch the value off of the stack with the given offset from the top slot in the frame.
	 *
	 * @param frame
	 * 		Frame to look at.
	 * @param offset
	 * 		Offset from top to fetch.
	 * @param <T>
	 * 		Type to return.
	 *
	 * @return Value at offset from the top of the stack.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getStackValueFromTop(Frame<AbstractValue> frame, int offset) {
		return (T) frame.getStack(frame.getStackSize() - (1 + offset)).getValue();
	}

	/**
	 * Fetch the top {@code N} items of the stack, where {@code N = argCount}.
	 *
	 * @param frame
	 * 		Frame to look at.
	 * @param argCount
	 * 		Number of items off of the stack to pull.
	 *
	 * @return Array of values from the stack.
	 */
	public static Object[] getStackArguments(Frame<AbstractValue> frame, int argCount) {
		Object[] args = new Object[argCount];
		for (int i = 0; i < argCount; i++)
			args[i] = getStackValueFromTop(frame, (argCount - i) - 1);
		return args;
	}
}
