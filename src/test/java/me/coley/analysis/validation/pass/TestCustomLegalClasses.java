package me.coley.analysis.validation.pass;

import me.coley.analysis.TestUtils;
import me.coley.analysis.util.FrameUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import static org.junit.jupiter.api.Assertions.*;

public class TestCustomLegalClasses extends TestUtils {
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
}
