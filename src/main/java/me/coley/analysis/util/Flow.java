package me.coley.analysis.util;

import java.util.Objects;

/**
 * Temporary control flow to record {@link org.objectweb.asm.tree.analysis.Analyzer#newControlFlowEdge(int, int)}.
 *
 * @author Matt Coley
 */
public class Flow {
	private final int from;
	private final int to;

	/**
	 * @param from
	 * 		From insn index.
	 * @param to
	 * 		To insn index.
	 */
	public Flow(int from, int to) {
		this.from = from;
		this.to = to;
	}

	/**
	 * @return From insn index.
	 */
	public int getFrom() {
		return from;
	}

	/**
	 * @return To insn index.
	 */
	public int getTo() {
		return to;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Flow flow = (Flow) o;
		return from == flow.from && to == flow.to;
	}

	@Override
	public int hashCode() {
		return Objects.hash(from, to);
	}

	@Override
	public String toString() {
		return "Flow{" + from + " ==> " + to + '}';
	}
}
