package me.coley.analysis.validation.pass;

import me.coley.analysis.TestUtils;
import me.coley.analysis.util.FrameUtil;
import me.coley.analysis.value.AbstractValue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;

import static org.junit.jupiter.api.Assertions.*;

public class TestCustomLegalClasses extends TestUtils {
	@BeforeAll
	public static void setup() {
		setupVirtualLookups();
	}

	@ParameterizedTest
	@ValueSource(strings =  {
			"bin/custom/misc/JumpOverBadThrow.class",
			"bin/custom/misc/JumpOverUnusedCode.class",
			"bin/custom/misc/HelloWorldGotoOrdering.class",
			"bin/custom/misc/HelloWorldSwapOrdering.class",
			"bin/custom/misc/YodaHelloWorld.class",
			"bin/custom/misc/OpaqueYodaHelloWorld.class",
	})
	public void testClasses(String classPath) {
		ClassNode node = getFromName(classPath);
		for (MethodNode mn : node.methods)
			assertDoesNotThrow(() -> FrameUtil.getFrames(node.name, mn));
	}

	@ParameterizedTest
	@ValueSource(strings =  {
			"bin/custom/misc/JumpOverUnusedCode.class",
	})
	public void testClassexs(String classPath) throws AnalyzerException {
		ClassNode node = getFromName(classPath);
		for (MethodNode mn : node.methods)
		{
			Frame<AbstractValue>[] frames = FrameUtil.getFrames(node.name, mn);
			System.out.println(":)");
		}
	}
}
