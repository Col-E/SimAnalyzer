package me.coley.analysis;

import org.junit.jupiter.api.Assertions;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Some common utilities.
 */
public class TestUtils {
	/**
	 * @param file
	 * 		Path to file in classpath.
	 *
	 * @return File reference.
	 *
	 * @throws IOException
	 * 		Thrown if the URL to the file could not be created.
	 */
	public static File getClasspathFile(String file) throws IOException {
		return new File(URLDecoder.decode(getClasspathUrl(file).getFile(), "UTF-8"));
	}

	/**
	 * @param file
	 * 		Path to file in classpath.
	 *
	 * @return URL reference.
	 */
	public static URL getClasspathUrl(String file) {
		ClassLoader classLoader = TestUtils.class.getClassLoader();
		return classLoader.getResource(file);
	}

	/**
	 * @param path
	 * 		Path to class in test resources.
	 *
	 * @return Class node.
	 */
	public static ClassNode getFromName(String path) {
		return getFromName(path, ClassReader.SKIP_FRAMES);
	}

	/**
	 * @param path
	 * 		Path to class in test resources.
	 * @param readFlags
	 * 		Flags to pass to {@link ClassReader}.
	 *
	 * @return Class node.
	 */
	public static ClassNode getFromName(String path, int readFlags) {
		try {
			return getFromBytes(Files.readAllBytes(getClasspathFile(path).toPath()), readFlags);
		} catch (IOException ex) {
			Assertions.fail(ex);
			throw new IllegalStateException();
		}
	}

	/**
	 * @param clazz
	 * 		Bytecode of class.
	 *
	 * @return Class node.
	 */
	public static ClassNode getFromBytes(byte[] clazz) {
		return getFromBytes(clazz, ClassReader.SKIP_FRAMES);
	}

	/**
	 * @param clazz
	 * 		Bytecode of class.
	 * @param readFlags
	 * 		Flags to pass to {@link ClassReader}.
	 *
	 * @return Class node.
	 */
	public static ClassNode getFromBytes(byte[] clazz, int readFlags) {
		try {
			ClassReader cr = new ClassReader(clazz);
			ClassNode node = new ClassNode();
			cr.accept(node, readFlags);
			return node;
		} catch (Throwable t) {
			Assertions.fail(t);
			throw new IllegalStateException();
		}
	}

	/**
	 * Fetch a method by its name.
	 *
	 * @param node
	 * 		Class containing the method.
	 * @param name
	 * 		Name of method.
	 *
	 * @return Method node inclass.
	 */
	public static MethodNode getMethod(ClassNode node, String name) {
		for (MethodNode mn : node.methods)
			if (mn.name.endsWith(name))
				return mn;
		Assertions.fail("No method by name '" + name + "' in class: " + node.name);
		throw new IllegalStateException();
	}

	/**
	 * @param method
	 * 		Method to parse. Requires {@link ClassReader#EXPAND_FRAMES}.
	 *
	 * @return Method's stack frames.
	 */
	public static NavigableMap<Integer, FrameNode> getStackFrames(MethodNode method) {
		NavigableMap<Integer, FrameNode> stackFrames = new TreeMap<>();
		for (int i = 0; i < method.instructions.size(); i++) {
			AbstractInsnNode instruction = method.instructions.get(i);
			if (instruction instanceof FrameNode) {
				stackFrames.put(i, (FrameNode) instruction);
			}
		}
		return stackFrames;
	}

	/**
	 * @param insns
	 * 		Instructions list.
	 * @param name
	 * 		Method name.
	 *
	 * @return Index in instructions of method call with matching name.
	 */
	public static int getMethodCallIndex(InsnList insns, String name) {
		int i = 0;
		for (AbstractInsnNode insn : insns) {
			if (insn instanceof MethodInsnNode) {
				MethodInsnNode min = (MethodInsnNode) insn;
				if (min.name.endsWith(name))
					return i;
			}
			i++;
		}
		throw new IllegalStateException("No result");
	}

	/**
	 * Analyze and retch the frames of the given method, if it is valid.
	 *
	 * @param owner
	 * 		Name of method's defining class.
	 * @param method
	 * 		Method instance.
	 *
	 * @return Analyzed frames of the method.
	 *
	 * @throws AnalyzerException
	 * 		When analysis fails.
	 */
	public static SimFrame[] getFrames(String owner, MethodNode method) throws AnalyzerException {
		SimInterpreter interpreter = new SimInterpreter();
		SimAnalyzer analyzer = new SimAnalyzer(interpreter);
		SimFrame[] frames = analyzer.analyze(owner, method);
		if (interpreter.hasReportedProblems())
			Assertions.fail(interpreter.getProblemInsns().values().iterator().next());
		NavigableMap<Integer, FrameNode> stackFrames = TestUtils.getStackFrames(method);
		return frames;
	}
}
