package me.coley.analysis.simulation;

import me.coley.analysis.ParameterFactory;
import me.coley.analysis.SimAnalyzer;
import me.coley.analysis.SimInterpreter;
import me.coley.analysis.TestUtils;
import me.coley.analysis.value.AbstractValue;
import me.coley.analysis.value.PrimitiveValue;
import me.coley.analysis.value.simulated.StringSimulatedValue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;

import java.util.Collections;

// TODO: Expand with more obfuscation patterns to assert most common forms of string encryption can be simulated

public class TestStringDecrypt extends TestUtils {
	@Test
	@Disabled
	public void testCharXor() throws AnalyzerException {
		ClassNode node = getFromName("bin/javac/ExplodeStr.class");
		MethodNode method = getMethod(node, "xor");
		// Simulate method call with some arguments (String, int)
		SimInterpreter interpreter = new SimInterpreter();

		String text = "test";
		int key = 10;
		SimAnalyzer analyzer = new SimAnalyzer(interpreter) {
			@Override
			protected ParameterFactory createParameterFactory() {
				return (isInstanceMethod, local, type) -> {
					if (local == 0) {
						return StringSimulatedValue.of(Collections.emptyList(), createTypeChecker(), text);
					} else if (local == 1) {
						return PrimitiveValue.ofInt(Collections.emptyList(), key);
					}
					return null;
				};
			}
		};

		Frame<AbstractValue>[] frames = analyzer.analyze(node.name, method);
		System.out.println(frames.length);
	}
}
