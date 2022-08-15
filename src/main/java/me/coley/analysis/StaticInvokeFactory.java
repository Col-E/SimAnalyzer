package me.coley.analysis;

import me.coley.analysis.value.AbstractValue;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.List;

/**
 * Factory for generating {@link AbstractValue} from static method calls.
 *
 * @author Matt Coley
 */
public interface StaticInvokeFactory {
	/**
	 * @param insn
	 * 		Method instruction.
	 * @param arguments
	 * 		Arguments on the stack.
	 *
	 * @return Value of invoke. {@code null} for void types.
	 */
	AbstractValue invokeStatic(MethodInsnNode insn, List<? extends AbstractValue> arguments);
}
