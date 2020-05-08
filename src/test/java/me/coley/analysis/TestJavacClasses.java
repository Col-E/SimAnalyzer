package me.coley.analysis;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import static org.junit.jupiter.api.Assertions.*;

public class TestJavacClasses extends TestUtils {
	@BeforeAll
	public static void setup() {
		setupVirtualLookups();
	}

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
			assertDoesNotThrow(() -> assertNotNull(getFrames(node.name, mn)));
	}
}
