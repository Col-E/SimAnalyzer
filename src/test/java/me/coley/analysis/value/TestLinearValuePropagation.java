package me.coley.analysis.value;

import me.coley.analysis.SimFrame;
import me.coley.analysis.TestUtils;
import me.coley.analysis.util.FrameUtil;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;

import static org.junit.jupiter.api.Assertions.*;

public class TestLinearValuePropagation extends TestUtils {
	@Test
	public void testImmediate() throws AnalyzerException {
		ClassNode node = getFromName("bin/javac/HelloWorld.class");
		MethodNode method = getMethod(node, "helloSplit");
		SimFrame[] frames = TestUtils.getFrames(node.name, method);
		int index = getMethodCallIndex(method.instructions, "sayTwoWords");
		Object[] value = FrameUtil.getStackArgumentLiterals(frames[index], 2);
		assertEquals("Hello", value[0]);
		assertEquals("World", value[1]);
	}

	@Test
	public void testConcatStrings() throws AnalyzerException {
		ClassNode node = getFromName("bin/javac/HelloWorld.class");
		MethodNode method = getMethod(node, "helloVariables");
		SimFrame[] frames = TestUtils.getFrames(node.name, method);
		int index = getMethodCallIndex(method.instructions, "println");
		String value = FrameUtil.getTopStackLiteral(frames[index]);
		assertEquals("Hello World", value);
	}

	@Test
	public void testConcatStrings_GotoOrder() throws AnalyzerException {
		ClassNode node = getFromName("bin/custom/misc/HelloWorldGotoOrdering.class");
		MethodNode method = getMethod(node, "helloVariables");
		SimFrame[] frames = TestUtils.getFrames(node.name, method);
		int index = getMethodCallIndex(method.instructions, "println");
		String value = FrameUtil.getTopStackLiteral(frames[index]);
		assertEquals("Hello World", value);
	}

	@Test
	public void testConcatStrings_SwapOrder() throws AnalyzerException {
		ClassNode node = getFromName("bin/custom/misc/HelloWorldSwapOrdering.class");
		MethodNode method = getMethod(node, "helloVariables");
		SimFrame[] frames = TestUtils.getFrames(node.name, method);
		int index = getMethodCallIndex(method.instructions, "println");
		String value = FrameUtil.getTopStackLiteral(frames[index]);
		assertEquals("Hello World", value);
	}
}
