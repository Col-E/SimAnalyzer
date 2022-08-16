package me.coley.analysis.util;

import org.objectweb.asm.tree.AbstractInsnNode;

import java.lang.reflect.Field;

/**
 * Instruction level utilities.
 *
 * @author Matt Coley
 */
public class InsnUtil {
	private static final Field INSN_INDEX;

	/**
	 * Calculate the index of an instruction.
	 *
	 * @param ain
	 * 		instruction.
	 *
	 * @return Instruction index.
	 */
	public static int index(AbstractInsnNode ain) {
		try {
			int v = (int) INSN_INDEX.get(ain);
			// Can return -1
			if (v >= 0)
				return v;
		} catch(Exception ex) { /* Fail */ }
		// Fallback
		int index = 0;
		while(ain.getPrevious() != null) {
			ain = ain.getPrevious();
			index++;
		}
		return index;
	}

	static {
		try {
			INSN_INDEX = AbstractInsnNode.class.getDeclaredField("index");
			INSN_INDEX.setAccessible(true);
		} catch(Exception ex) {
			throw new IllegalStateException("Failed to get insn index field", ex);
		}
	}
}
