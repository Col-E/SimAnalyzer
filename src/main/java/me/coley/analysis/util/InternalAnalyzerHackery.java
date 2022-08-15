package me.coley.analysis.util;

import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.analysis.Analyzer;

import java.lang.reflect.Field;

/**
 * This exists because ASM makes extending parts of their impossible.
 * Hopefully I'm just dumb and there is a better way to go about this.
 * <br>
 * If you're reading this and know an alternative <b>PLEASE</b> open a PR.
 *
 * @author Matt Coley
 */
public class InternalAnalyzerHackery {
	private final Analyzer<?> analyzer;
	private final Field f_inInstructionsToProcess;
	private final Field f_instructionsToProcess;
	private final Field f_numInstructionsToProcess;

	/**
	 * @param analyzer
	 * 		The analyzer instance to hack.
	 */
	public InternalAnalyzerHackery(Analyzer<?> analyzer) {
		this.analyzer = analyzer;
		try {
			f_inInstructionsToProcess =
					Analyzer.class.getDeclaredField("inInstructionsToProcess");
			f_instructionsToProcess =
					Analyzer.class.getDeclaredField("instructionsToProcess");
			f_numInstructionsToProcess =
					Analyzer.class.getDeclaredField("numInstructionsToProcess");
			f_inInstructionsToProcess.setAccessible(true);
			f_instructionsToProcess.setAccessible(true);
			f_numInstructionsToProcess.setAccessible(true);
		} catch(Throwable t) {
			throw new IllegalStateException("Did the analyzer internals change?", t);
		}
	}

	/**
	 * Negates the internal ASM analyzer logic that visits the fall-through of a
	 * {@link JumpInsnNode}.
	 *
	 * @param fallthroughIndex
	 * 		Index in instructions where fallthrough starts.
	 * @param destination
	 * 		New destination for analyzer to continue analyzing at.
	 */
	public void stopAnalyzerFromGoingToFallthrough(int fallthroughIndex, LabelNode destination) {
		try {
			// inInstructionsToProcess[fallthroughIndex] = false;
			// instructionsToProcess[numInstructionsToProcess - 1] = destinationLabelIndex;
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
}
