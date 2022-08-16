package me.coley.analysis.util;

import me.coley.analysis.cfg.BlockHandler;
import me.coley.analysis.value.AbstractValue;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodNode;

import static org.objectweb.asm.Opcodes.IFNULL;

/**
 * Utilities for flow control.
 *
 * @author Matt Coley
 */
public class FlowUtil {
	/**
	 * @param method
	 * 		Method instance with instructions.
	 * @param insnIndex
	 * 		Supposed jump or table instruction.
	 * @param successorIndex
	 * 		Supposed destination label instruction.
	 *
	 * @return {@code true} when supposed values match expected types.
	 */
	public static boolean isFlowModifier(MethodNode method, int insnIndex, int successorIndex) {
		AbstractInsnNode dest = method.instructions.get(successorIndex);
		// Flow modifiers direct flow to labels
		if (dest.getType() != AbstractInsnNode.LABEL)
			return false;
		// Source must be a flow modifiying instruction
		AbstractInsnNode src = method.instructions.get(insnIndex);
		int srcType = src.getType();
		return srcType == AbstractInsnNode.JUMP_INSN ||
				srcType == AbstractInsnNode.LOOKUPSWITCH_INSN ||
				srcType == AbstractInsnNode.TABLESWITCH_INSN;
	}

	/**
	 * Check if the used instruction which uses some nullable value is safe to use.
	 *
	 * @param blockHandler
	 * 		Block handler to determine scope.
	 * @param value
	 * 		Value to check if it has been null checked.
	 * @param usage
	 * 		Instruction with potential action against a {@code null} value.
	 *
	 * @return {@code true} if the value has been null checked and is safe to use at the given usage instruction.
	 */
	public static boolean isNullChecked(BlockHandler blockHandler, AbstractValue value, AbstractInsnNode usage) {
		// If it is not-null we have no work to do
		if (!value.isNull())
			return true;
		// No null check, not safe
		JumpInsnNode nullCheck = value.getNullCheck();
		if (nullCheck == null)
			return false;
		// Fetch the "safe" instruction based off of the jump opcode
		AbstractInsnNode safeInsn = null;
		if (nullCheck.getOpcode() == IFNULL) {
			safeInsn = nullCheck.getNext();
		} else {
			safeInsn = nullCheck.label;
		}
		// Determine if the block containing the safe index also contains the usage instruction.
		int safeIndex = InsnUtil.index(safeInsn);
		return blockHandler.getBlockAtIndex(safeIndex).getInsns().contains(usage);
	}
}
