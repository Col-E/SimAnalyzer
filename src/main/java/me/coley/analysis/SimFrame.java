package me.coley.analysis;

import me.coley.analysis.value.AbstractValue;
import me.coley.analysis.value.UninitializedValue;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Interpreter;

import java.util.HashSet;
import java.util.Set;

/**
 * Frame for {@link AbstractValue} content.
 *
 * @author Matt
 */
public class SimFrame extends Frame<AbstractValue> {
	private final Set<Integer> reservedSlots = new HashSet<>();

	/**
	 * New frame of size.
	 *
	 * @param numLocals
	 * 		Maximum number of local variables of the frame.
	 * @param numStack
	 * 		Maximum stack size of the frame.
	 */
	public SimFrame(int numLocals, int numStack) {
		super(numLocals, numStack);
	}

	/**
	 * New frame based on given frame.
	 *
	 * @param frame
	 * 		Old frame.
	 */
	public SimFrame(final SimFrame frame) {
		super(frame);
	}

	@Override
	public void setLocal(int index, AbstractValue value) {
		if (value != UninitializedValue.UNINITIALIZED_VALUE) {
			// Check against reserved slots used by double and long locals
			if(reservedSlots.contains(index))
				throw new IllegalStateException("Cannot set local[" + index + "] " +
						"since it is reserved by a double/long (which reserves two slots)");
			if(value.getValue() instanceof Double || value.getValue() instanceof Long)
				reservedSlots.add(index + 1);
		}
		// Update local
		super.setLocal(index, value);
	}

	@Override
	public boolean merge(Frame<? extends AbstractValue> frame, Interpreter<AbstractValue> interpreter) throws AnalyzerException {
		return super.merge(frame, interpreter);
	}

	@Override
	public boolean merge(Frame<? extends AbstractValue> frame, boolean[] localsUsed) {
		return super.merge(frame, localsUsed);
	}
}
