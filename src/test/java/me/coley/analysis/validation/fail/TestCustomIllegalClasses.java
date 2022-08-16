package me.coley.analysis.validation.fail;

import me.coley.analysis.SimFrame;
import me.coley.analysis.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

public class TestCustomIllegalClasses extends TestUtils {
	@ParameterizedTest
	@ValueSource(strings = {
			"bin/custom/illegal/vars/DoubleStoreAsInt.class",
			"bin/custom/illegal/vars/IntToObject.class",
			"bin/custom/illegal/vars/LongToInt.class",
			"bin/custom/illegal/vars/StringToInt.class"
	})
	public void testClasses(String classPath) {
		ClassNode node = getFromName(classPath);
		for (MethodNode mn : node.methods)
			assertThrows(AnalyzerException.class, () -> TestUtils.getFrames(node.name, mn));
	}

	@Test
	public void testClassFailsInsteadOfInfiniteLooping() {
		// This class file was picked out of a program that originally caused an infinite loop in the analyzer
		// The bug has since been fixed, but this will track for regression.
		ClassNode node = getFromName("bin/custom/illegal/flow/ConfusingJavacFlow.class");
		for (MethodNode mn : node.methods)
			assertTimeoutPreemptively(Duration.ofMillis(500),
					() -> assertThrows(AnalyzerException.class,
							() -> {
								SimFrame[] frames = TestUtils.getFrames(node.name, mn);
								System.out.println(frames);
							}));
	}
}
