package me.coley.analysis.validation.pass;

import me.coley.analysis.TestUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import static org.junit.jupiter.api.Assertions.*;

public class TestJavacClasses extends TestUtils {
	@ParameterizedTest
	@ValueSource(strings =  {
			"bin/javac/HelloWorld.class",
			"bin/javac/ExplodeStr.class",
			"bin/javac/PrimitiveCasting.class",
			"bin/javac/PrimitiveMath.class",
			"bin/javac/FindNArray.class",
			"bin/javac/SetItToNull.class",
			"bin/javac/ZipIO.class",
			"bin/javac/Type.class",
			"bin/javac/StringEquals.class"
	})
	public void testClasses(String classPath) {
		ClassNode node = getFromName(classPath);
		for (MethodNode mn : node.methods)
			assertDoesNotThrow(() -> assertNotNull(TestUtils.getFrames(node.name, mn)));
	}
}
