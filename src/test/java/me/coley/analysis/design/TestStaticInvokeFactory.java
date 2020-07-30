package me.coley.analysis.design;

import me.coley.analysis.SimAnalyzer;
import me.coley.analysis.SimInterpreter;
import me.coley.analysis.StaticInvokeFactory;
import me.coley.analysis.TestUtils;
import me.coley.analysis.util.FrameUtil;
import me.coley.analysis.value.AbstractValue;
import me.coley.analysis.value.simulated.StringValue;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestStaticInvokeFactory extends TestUtils {
	@Test
	public void testUnknownByDefault() throws AnalyzerException {
		ClassNode node = getFromName("bin/javac/HelloWorld.class");
		MethodNode mn = getMethod(node, "helloFromGet");
		SimAnalyzer analyzer = new SimAnalyzer(new SimInterpreter());
		Frame<AbstractValue>[] frames = analyzer.analyze(node.name, mn);
		int call = getMethodCallIndex(mn.instructions, "println");
		AbstractValue value = FrameUtil.getTopStack(frames[call]);
		// The "Hello World" text is loaded from a INVOKESTATIC call, so its unknown by default
		assertTrue(value.isValueUnresolved());
	}

	@Test
	public void testUsesCustomValue() throws AnalyzerException {
		ClassNode node = getFromName("bin/javac/HelloWorld.class");
		MethodNode mn = getMethod(node, "helloFromGet");
		SimAnalyzer analyzer = new SimAnalyzer(new SimInterpreter()) {
			@Override
			protected StaticInvokeFactory createStaticInvokeFactory() {
				return new StaticInvokeFactoryTestImpl();
			}
		};
		Frame<AbstractValue>[] frames = analyzer.analyze(node.name, mn);
		int call = getMethodCallIndex(mn.instructions, "println");
		AbstractValue value = FrameUtil.getTopStack(frames[call]);
		// The "Hello World" text is loaded from a INVOKESTATIC call, but we have a handler for that.
		// We specify the return-value of the static call is "Hello World"
		assertTrue(value.isValueResolved());
		assertEquals("Hello World", value.getValue());
	}

	static class StaticInvokeFactoryTestImpl implements StaticInvokeFactory {
		@Override
		public AbstractValue invokeStatic(MethodInsnNode insn, List<?
				extends AbstractValue> arguments) {
			if (insn.name.equals("getHello")) {
				return StringValue.of((List<AbstractInsnNode>) null, null, "Hello World");
			}
			return null;
		}
	}
}
