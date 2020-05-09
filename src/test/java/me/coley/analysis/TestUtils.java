package me.coley.analysis;

import org.junit.jupiter.api.Assertions;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;

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
		try {
			ClassReader cr = new ClassReader(Files.readAllBytes(getClasspathFile(path).toPath()));
			ClassNode node = new ClassNode();
			cr.accept(node, ClassReader.SKIP_FRAMES);
			return node;
		} catch(IOException ex) {
			Assertions.fail(ex);
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
		for(MethodNode mn : node.methods)
			if (mn.name.endsWith(name))
				return mn;
		Assertions.fail("No method by name '" + name + "' in class: " + node.name);
		throw new IllegalStateException();
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
		for(AbstractInsnNode insn : insns) {
			if (insn instanceof MethodInsnNode) {
				MethodInsnNode min = (MethodInsnNode) insn;
				if (min.name.endsWith(name))
					return i;
			}
			i++;
		}
		throw new IllegalStateException("No result");
	}
}
