package me.coley.analysis;

import me.coley.analysis.value.AbstractValue;
import me.coley.analysis.value.UninitializedValue;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Interpreter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static me.coley.analysis.util.CollectUtils.disjoint;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;

/**
 * Frame for {@link AbstractValue} content.
 *
 * @author Matt Coley
 */
public class SimFrame extends Frame<AbstractValue> {
	private final Set<Integer> reservedSlots = new HashSet<>();
	private final Set<SimFrame> flowInputs = new HashSet<>();
	private final Set<SimFrame> flowOutputs = new HashSet<>();
	private AbstractInsnNode instruction;

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
	public void execute(AbstractInsnNode insn, Interpreter<AbstractValue> interpreter) throws AnalyzerException {
		if (insn.getOpcode() == Opcodes.SWAP) {
			// For tracking purposes, if we SWAP with another instruction we have to be aware of everything that
			// contributes to BOTH items in the SWAP operation.
			AbstractValue value2 = pop();
			AbstractValue value1 = pop();
			if (value1.getSize() != 1 || value2.getSize() != 1) {
				throw new AnalyzerException(insn, "Illegal use of SWAP");
			}
			value2 = value2.copy(insn);
			value1 = value1.copy(insn);
			// Have both now aware of each other's contributing instructions
			List<AbstractInsnNode> addTo1 = disjoint(value2.getInsns(), value1.getInsns());
			List<AbstractInsnNode> addTo2 = disjoint(value1.getInsns(), value2.getInsns());
			value1.addContributing(addTo1);
			value2.addContributing(addTo2);
			push(value2);
			push(value1);
		} else if (insn.getOpcode() == Opcodes.DUP) {
			// TODO: Will need to create custom handling of other similar behaving instructions

			// For tracking purposes, we need our DUP to be tracked as a contributing instruction in BOTH instances.
			// The original and in the duplicate.
			AbstractValue value1 = pop();
			if (value1.getSize() != 1) {
				throw new AnalyzerException(insn, "Illegal use of DUP");
			}
			// Have both copies be aware of the DUP
			push(value1.copy(insn));
			push(value1.copy(insn));
		} else {
			super.execute(insn, interpreter);
			// Ensure constructor calls are tracked by values that should be the 'owner' of the call.
			if (insn.getOpcode() == INVOKESPECIAL) {
				MethodInsnNode min = (MethodInsnNode) insn;
				if (min.name.equals("<init>")) {
					AbstractValue ownerValue = getStack(getStackSize() - 1);
					if (ownerValue.isReference() && min.owner.equals(ownerValue.getType().getInternalName())) {
						ownerValue.addContributing(min);
					}
				}
			}
		}
	}

	@Override
	public void setLocal(int index, AbstractValue value) {
		if (value != UninitializedValue.UNINITIALIZED_VALUE) {
			// Check against reserved slots used by double and long locals
			if (reservedSlots.contains(index))
				throw new IllegalStateException("Cannot set local[" + index + "] " +
						"since it is reserved by a double/long (which reserves two slots)");
			if (value.getValue() instanceof Double || value.getValue() instanceof Long)
				reservedSlots.add(index + 1);
		}
		// Update local
		super.setLocal(index, value);
	}

	@Override
	public void initJumpTarget(int opcode, LabelNode target) {
		reservedSlots.clear();
	}

	/**
	 * @return Instruction of this frame. Executing it will result in the stack/locals state seen in
	 * the next {@link SimFrame}.
	 * <br>
	 * For example {@code ACONST_NULL} would yield a {@code null} appearing in the next frame.
	 */
	public AbstractInsnNode getInstruction() {
		return instruction;
	}

	/**
	 * @return Frames that flow into this one.
	 */
	public Set<SimFrame> getFlowInputs() {
		return flowInputs;
	}

	/**
	 * @return Frames this flows into.
	 */
	public Set<SimFrame> getFlowOutputs() {
		return flowOutputs;
	}

	/**
	 * Called by {@link SimAnalyzer#analyze(String, MethodNode)}.
	 *
	 * @param instruction
	 * 		Instruction of this frame.
	 */
	public void setInstruction(AbstractInsnNode instruction) {
		this.instruction = instruction;
	}

	/**
	 * Called by {@link SimAnalyzer#analyze(String, MethodNode)}.
	 *
	 * @param to
	 * 		Frame this one flows into.
	 */
	public void flowsInto(SimFrame to) {
		flowOutputs.add(to);
		to.flowInputs.add(this);
	}
}
