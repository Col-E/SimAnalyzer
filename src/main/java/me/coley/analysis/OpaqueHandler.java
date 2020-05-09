package me.coley.analysis;

import me.coley.analysis.util.InsnUtil;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.analysis.Analyzer;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static me.coley.analysis.OpaquePredicateType.*;

/**
 * Opaque predicate manager.
 *
 * @author Matt
 */
public class OpaqueHandler {
	private final Map<AbstractInsnNode, OpaquePredicateType> opaqueJumpMap = new HashMap<>();
	private final SimAnalyzer analyzer;
	private boolean hasHitOpaquePredicate;
	private boolean doesOpaqueJumpGotoDestination;
	private LabelNode destination;

	/**
	 * Initialize handler.
	 *
	 * @param analyzer
	 * 		Owner analyzer.
	 */
	public OpaqueHandler(SimAnalyzer analyzer) {
		this.analyzer = analyzer;
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
			stopAnalyzerFromGoingToFallthrough(fallthroughIndex);
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
	 * Negates the internal ASM analyzer logic that visits the fall-through of a
	 * {@link JumpInsnNode}.
	 *
	 * @param fallthroughIndex
	 * 		Index in instructions where fallthrough starts.
	 */
	private void stopAnalyzerFromGoingToFallthrough(int fallthroughIndex) {
		try {
			// inInstructionsToProcess[fallthroughIndex] = false;
			// instructionsToProcess[numInstructionsToProcess - 1] = destinationLabelIndex;
			Field f_inInstructionsToProcess = Analyzer.class.getDeclaredField(
					"inInstructionsToProcess");
			Field f_instructionsToProcess = Analyzer.class.getDeclaredField(
					"instructionsToProcess");
			Field f_numInstructionsToProcess = Analyzer.class.getDeclaredField(
					"numInstructionsToProcess");
			f_inInstructionsToProcess.setAccessible(true);
			f_instructionsToProcess.setAccessible(true);
			f_numInstructionsToProcess.setAccessible(true);
			boolean[] inInstructionsToProcess =
					(boolean[]) f_inInstructionsToProcess.get(analyzer);
			int[] instructionsToProcess = (int[]) f_instructionsToProcess.get(analyzer);
			int numInstructionsToProcess = (int) f_numInstructionsToProcess.get(analyzer);
			inInstructionsToProcess[fallthroughIndex] = false;
			instructionsToProcess[numInstructionsToProcess - 1] = InsnUtil.index(destination);
		} catch(Throwable t) {
			throw new IllegalStateException("Did the analyzer internals change?", t);
		}
	}

	/**
	 * @return Map of instructions to their opaque predicate types.
	 */
	public Map<AbstractInsnNode, OpaquePredicateType> getOpaqueJumpMap() {
		return opaqueJumpMap;
	}
}
