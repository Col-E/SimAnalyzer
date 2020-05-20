package me.coley.analysis.cfg;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// TODO: Inbound / outbound

/**
 * Basic block of a control flow graph.
 *
 * @author Matt
 */
public class Block implements Comparable<Block> {
	private final int key;
	// Exclusive range
	private final int from;
	private final int to;
	// Inclusive range'd instructions
	private final List<AbstractInsnNode> insns;
	// Misc
	private final List<Block> subBlocks = new ArrayList<>();
	private Block parent;
	private int depth;

	private Block(int from, int to, List<AbstractInsnNode> insns) {
		this.from = from;
		this.to = to;
		this.insns = insns;
		key = Math.max(from, to);
	}

	/**
	 * @param method
	 * 		Method containing instructions.
	 * @param from
	 * 		Exclusive range start.
	 * @param to
	 * 		Exclusive range end.
	 *
	 * @return Block representing the range.
	 */
	public static Block create(MethodNode method, int from, int to) {
		List<AbstractInsnNode> insns = new ArrayList<>();
		for (int i = from + 1; i < to - 1; i++)
			insns.add(method.instructions.get(i));
		return new Block(from, to, insns);
	}

	/**
	 * @param insnIndex
	 * 		Instruction index.
	 *
	 * @return Sub block that contains the given index. If no sub block matches the range, the
	 * current block.
	 */
	public Block getBlockFromIndex(int insnIndex) {
		for (Block block : getSubBlocks())
			if (insnIndex > block.getFrom() && insnIndex < block.getTo())
				return block.getBlockFromIndex(insnIndex);
		return this;
	}

	/**
	 * @return Exclusive range start of the block.
	 */
	public int getFrom() {
		return from;
	}

	/**
	 * @return Exclusive range end of the block.
	 */
	public int getTo() {
		return to;
	}

	/**
	 * @return All instructions in the block.
	 */
	public List<AbstractInsnNode> getInsns() {
		return insns;
	}

	/**
	 * @return First instruction in the block.
	 */
	public AbstractInsnNode getFirst() {
		return insns.get(0);
	}

	/**
	 * @return Last instruction in the block.
	 */
	public AbstractInsnNode getLast() {
		return insns.get(insns.size() - 1);
	}

	/**
	 * @return Blocks contained within this block.
	 */
	public List<Block> getSubBlocks() {
		return subBlocks;
	}

	/**
	 * @param block
	 * 		Block to add to this one. Range is within the current.
	 */
	public void addSubBlock(Block block) {
		// Check if block can belong to a sub-block
		Block found = null;
		for (Block sub : getSubBlocks()) {
			// Assert the block's "from" index resides within the sub block
			if (block.getFrom() >= sub.getFrom() && block.getFrom() <= sub.getTo()) {
				found = sub;
			}
		}
		if (found != null) {
			// Try one level deeper
			found.addSubBlock(block);
			return;
		}
		// Check if block should contain an existing block
		for (Block sub : getSubBlocks()) {
			// Assert the sub's "from" index resides within the new block
			if (sub.getFrom() >= block.getFrom() && sub.getFrom() <= block.getTo()) {
				found = sub;
			}
		}
		if (found != null) {
			// New block should engulf the existing sub-block
			subBlocks.remove(found);
			block.addSubBlock(found);
		}
		// Add block to current block
		block.setParent(this);
		subBlocks.add(block);
		Collections.sort(subBlocks);
	}

	/**
	 * @return Parent block.
	 */
	public Block getParent() {
		return parent;
	}

	/**
	 * @param parent
	 * 		Parent block.
	 */
	public void setParent(Block parent) {
		this.parent = parent;
		// Calculate depth
		int depth = 0;
		Block tmp = this;
		while(tmp.getParent() != null) {
			tmp = tmp.getParent();
			depth++;
		}
		this.depth = depth;
	}

	/**
	 * @return Depth
	 */
	public int getDepth() {
		return depth;
	}

	/**
	 * @return {@code true} when block is root.
	 */
	public boolean isRoot() {
		return depth == 0;
	}

	@Override
	public String toString() {
		String prefix = "";
		if (getParent() == null)
			prefix =  "Root ";
		return prefix + "(" + from + "," + to + ")";
	}

	@Override
	public int compareTo(Block other) {
		return Integer.compare(key, other.key);
	}
}
