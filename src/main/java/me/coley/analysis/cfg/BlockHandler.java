package me.coley.analysis.cfg;

import org.objectweb.asm.tree.MethodNode;

/**
 * Manager of basic control-flow blocks.
 *
 * @author Matt Coley
 */
public class BlockHandler {
	private Block root;
	private MethodNode method;

	/**
	 * Effectively the constructor.
	 *
	 * @param method
	 * 		The method for this handler to manage.
	 */
	public void setMethod(MethodNode method) {
		this.method = method;
		root = createBlock(0, method.instructions.size() - 1);
	}

	/**
	 * Create and add a new block with the given range.
	 *
	 * @param insnIndex
	 * 		Block start range.
	 * @param successorIndex
	 * 		Block end range.
	 */
	public void add(int insnIndex, int successorIndex) {
		root.addSubBlock(createBlock(insnIndex, successorIndex));
	}

	/**
	 * @param index
	 * 		Some instruction index.
	 *
	 * @return Deepest block containing the index.
	 */
	public Block getBlockAtIndex(int index) {
		return root.getBlockFromIndex(index);
	}

	/**
	 * @param first
	 * 		First instruction index.
	 * @param second
	 * 		Second instruction index.
	 *
	 * @return Common block between two instruction indices.
	 */
	public Block getCommonBlock(int first, int second) {
		Block firstBlock = getBlockAtIndex(first);
		Block secondBlock = getBlockAtIndex(second);
		while(firstBlock != root) {
			while(firstBlock.getDepth() > secondBlock.getDepth())
				firstBlock = firstBlock.getParent();
			if (firstBlock == secondBlock)
				return firstBlock;
			secondBlock = secondBlock.getParent();
		}
		return null;
	}

	/**
	 * @param insnIndex
	 * 		Instruction index.
	 * @param successorIndex
	 * 		End index.
	 *
	 * @return Created block
	 */
	private Block createBlock(int insnIndex, int successorIndex) {
		int start = Math.min(insnIndex, successorIndex);
		int end = Math.max(insnIndex, successorIndex);
		return Block.create(method, start, end);
	}
}
