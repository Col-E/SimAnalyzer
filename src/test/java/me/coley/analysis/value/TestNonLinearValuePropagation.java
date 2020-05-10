package me.coley.analysis.value;

import me.coley.analysis.TestUtils;
import me.coley.analysis.util.FrameUtil;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestNonLinearValuePropagation extends TestUtils {
	@Test
	public void testGotoOrdering() throws AnalyzerException {
		// The println call occurs earlier in the method code than the actual loading
		// "Hello", "World" are loaded into variables in a non-linear flow using GOTO
		// The analyzer will properly handle flow to detect the value of "Hello World"
		ClassNode node = getFromName("bin/custom/misc/HelloWorldGotoOrdering.class");
		MethodNode method = getMethod(node, "helloVariables");
		Frame<AbstractValue>[] frames = TestUtils.getFrames(node.name, method);
		int index = getMethodCallIndex(method.instructions, "println");
		assertEquals("Hello World", FrameUtil.getTopStackLiteral(frames[index]));
	}

	@Test
	public void testSwapOrdering() throws AnalyzerException {
		// The println call occurs earlier in the method code than the actual loading
		// "Hello", "World" are loaded onto the stack in a non-linear flow using GOTO
		// The variables are put in the correct order for concatination by using SWAP
		// The analyzer will properly handle flow to detect the value of "Hello World"
		ClassNode node = getFromName("bin/custom/misc/HelloWorldSwapOrdering.class");
		MethodNode method = getMethod(node, "helloVariables");
		Frame<AbstractValue>[] frames = TestUtils.getFrames(node.name, method);
		int index = getMethodCallIndex(method.instructions, "println");
		assertEquals("Hello World", FrameUtil.getTopStackLiteral(frames[index]));
	}
}
