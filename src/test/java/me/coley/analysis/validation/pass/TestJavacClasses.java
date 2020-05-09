package me.coley.analysis.validation.pass;

import me.coley.analysis.TestUtils;
import me.coley.analysis.util.FrameUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import static org.junit.jupiter.api.Assertions.*;

public class TestJavacClasses extends TestUtils {
	@ParameterizedTest
	@ValueSource(strings =  {
			"bin/javac/HelloWorld.class",
			"bin/javac/PrimitiveCasting.class",
			"bin/javac/PrimitiveMath.class",
			"bin/javac/FindNArray.class",
			"bin/javac/ZipIO.class"
	})
	public void testClasses(String classPath) {
		ClassNode node = getFromName(classPath);
		for (MethodNode mn : node.methods)
			assertDoesNotThrow(() -> assertNotNull(FrameUtil.getFrames(node.name, mn)));
	}
}
