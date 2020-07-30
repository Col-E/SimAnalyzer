package me.coley.analysis.instruction;

import me.coley.analysis.TestUtils;
import me.coley.analysis.util.FrameUtil;
import me.coley.analysis.value.AbstractValue;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestInstructionTracking extends TestUtils {
	@Test
	public void testConcatStrings_SwapOrder() throws AnalyzerException {
		ClassNode node = getFromName("bin/custom/misc/HelloWorldSwapOrdering.class");
		MethodNode method = getMethod(node, "helloVariables");
		Frame<AbstractValue>[] frames = TestUtils.getFrames(node.name, method);
		int index = getMethodCallIndex(method.instructions, "println");
		// TODO: The DUP/InvokeSpecial should be included because they act on the value that was created
		//       that contributes
		// The following is the disassembly of the method "helloVariables"
		//
		// Lines prefixed with ">>>>>>>>>>>>>" indicate they are contributing instructions
		// to the value "Hello World" at the given frame index.
		//
		// GOTO C
		// A:
		// >>>>>>>>>>>>> SWAP
		// >>>>>>>>>>>>> NEW java/lang/StringBuilder
		// DUP
		// INVOKESPECIAL java/lang/StringBuilder.<init>()V
		// >>>>>>>>>>>>> SWAP
		// >>>>>>>>>>>>> INVOKEVIRTUAL java/lang/StringBuilder.append(Ljava/lang/String;)Ljava/lang/StringBuilder;
		// >>>>>>>>>>>>> LDC " "
		// >>>>>>>>>>>>> INVOKEVIRTUAL java/lang/StringBuilder.append(Ljava/lang/String;)Ljava/lang/StringBuilder;
		// >>>>>>>>>>>>> SWAP
		// >>>>>>>>>>>>> INVOKEVIRTUAL java/lang/StringBuilder.append(Ljava/lang/String;)Ljava/lang/StringBuilder;
		// >>>>>>>>>>>>> INVOKEVIRTUAL java/lang/StringBuilder.toString()Ljava/lang/String;
		// GETSTATIC java/lang/System.out Ljava/io/PrintStream;
		// >>>>>>>>>>>>> SWAP
		// INVOKEVIRTUAL java/io/PrintStream.println(Ljava/lang/String;)V
		// GOTO D
		// B:
		// >>>>>>>>>>>>> LDC "World"
		// GOTO A
		// ATHROW
		// C:
		// >>>>>>>>>>>>> LDC "Hello"
		// GOTO B
		// D:
		// RETURN
		AbstractValue value = FrameUtil.getTopStack(frames[index]);
		List<AbstractInsnNode> insns = value.getInsns();
		assertEquals(12, insns.size());
		assertEquals("Hello World", value.getValue());
	}
}
