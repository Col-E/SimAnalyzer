package me.coley.analysis;

import me.coley.analysis.util.InternalAnalyzerHackery;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;

import java.util.HashMap;
import java.util.Map;

import static me.coley.analysis.OpaquePredicateType.FALL_THROUGH;
import static me.coley.analysis.OpaquePredicateType.GOTO_DESTINATION;

/**
 * Opaque predicate manager.
 *
 * @author Matt Coley
 */
public class OpaqueHandler {
	private final Map<AbstractInsnNode, OpaquePredicateType> opaqueJumpMap = new HashMap<>();
	private final InternalAnalyzerHackery hackery;
	private boolean hasHitOpaquePredicate;
	private boolean doesOpaqueJumpGotoDestination;
	private LabelNode destination;

	/**
	 * Initialize handler.
	 *
	 * @param hackery
	 * 		Analyzer hacker.
	 */
	public OpaqueHandler(InternalAnalyzerHackery hackery) {
		this.hackery = hackery;
	}

	/**
	 * Reset state.
	 */
	public void reset() {
		hasHitOpaquePredicate = false;
		doesOpaqueJumpGotoDestination = false;
		opaqueJumpMap.clear();
		destination = null;
	}

	/**
	 * Called when a new control flow edge is visited. This occurs immediately after visiting
	 * opaque predicates, so we use this to intercept ASM's CFG handling.
	 *
	 * @param insnIndex
	 * 		New flow edge index.
	 * @param successorIndex
	 * 		Prior edge index.
	 */
	public void onVisitControlFlowEdge(int insnIndex, int successorIndex) {
		if (hasHitOpaquePredicate && doesOpaqueJumpGotoDestination) {
			int fallthroughIndex = insnIndex + 1;
			hackery.stopAnalyzerFromGoingToFallthrough(fallthroughIndex, destination);
		}
		// Reset opaque predicate marker
		hasHitOpaquePredicate = false;
	}

	/**
	 * @param insn
	 * 		Instruction that serves as an opaque predicate.
	 * @param gotoDestination
	 *        {@code true} when the opaque predicate jumps to the destination. {@code false} when
	 * 		it falls through.
	 */
	public void setOpaqueJump(AbstractInsnNode insn, boolean gotoDestination) {
		// Update map of <jump-insn, do-jump>
		opaqueJumpMap.put(insn, gotoDestination ? GOTO_DESTINATION : FALL_THROUGH);
		// Update mark opaque predicate flags
		this.hasHitOpaquePredicate = true;
		this.doesOpaqueJumpGotoDestination = gotoDestination;
		if (insn instanceof JumpInsnNode)
			this.destination = ((JumpInsnNode) insn).label;
	}

	/**
	 * @return Map of instructions to their opaque predicate types.
	 */
	public Map<AbstractInsnNode, OpaquePredicateType> getOpaqueJumpMap() {
		return opaqueJumpMap;
	}
}
