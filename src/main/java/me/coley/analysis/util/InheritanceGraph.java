package me.coley.analysis.util;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.*;
import java.util.zip.*;

import org.objectweb.asm.ClassReader;


/**
 * Simple class inheritance graph.
 *
 * @author Matt
 */
public class InheritanceGraph {
	private static final String MAP_KV_SPLIT = ":::";
	private static final String MAP_VAL_SPLIT = ",";
	private SetMap<String, String> parentsOf = new SetMap<>();
	private SetMap<String, String> childrenOf = new SetMap<>();
	private SetMap<String, String> parentsOfCachedAll = new SetMap<>();
	private SetMap<String, String> childrenOfCachedAll = new SetMap<>();

	/**
	 * @return Copied instance.
	 */
	public InheritanceGraph copy() {
		InheritanceGraph copy = new InheritanceGraph();
		copy.parentsOf.putAll(parentsOf);
		copy.childrenOf.putAll(childrenOf);
		copy.parentsOfCachedAll.putAll(parentsOfCachedAll);
		copy.childrenOfCachedAll.putAll(childrenOfCachedAll);
		return copy;
	}

	/**
	 * Add classes from the current classpath to the inheritance graph.
	 *
	 * @throws IOException
	 * 		When a classpath item cannot be added.
	 */
	public void addClasspath() throws IOException {
		String path = System.getProperty("java.class.path");
		String separator = System.getProperty("path.separator");
		String localDir = System.getProperty("user.dir");
		if (path != null && !path.isEmpty()) {
			String[] items = path.split(separator);
			for (String item : items) {
				Path filePath = Paths.get(item);
				boolean isAbsolute = filePath.isAbsolute();
				File file;
				if (isAbsolute)
					file = new File(item);
				else
					file = Paths.get(localDir, item).toFile();
				if (!file.exists())
					continue;
				if (file.isDirectory())
					addDirectory(file);
				else if (file.getName().endsWith(".jar") || file.getName().endsWith(".jmod"))
					addArchive(file);
			}
		}
	}

	/**
	 * Add classes from the given directory to the inheritance graph.
	 *
	 * @param dir
	 * 		Directory to use.
	 *
	 * @throws IOException
	 * 		When walking the directory fails
	 */
	public void addDirectory(File dir) throws IOException {
		if (!dir.exists())
			return;
		Files.walkFileTree(Paths.get(dir.getAbsolutePath()), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				if (file.toString().endsWith(".class"))
					addClass(file.toFile());
				else if (file.toString().endsWith(".jar") || file.toString().endsWith(".jmod"))
					addArchive(file.toFile());
				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * Add classes from the given archive to the inheritance graph.
	 *
	 * @param archive
	 * 		Archive to use.
	 *
	 * @throws IOException
	 * 		When reading classes from the archive fails.
	 */
	public void addArchive(File archive) throws IOException {
		try (ZipFile jarArchive = new ZipFile(archive)) {
			byte[] data = new byte[1024];
			for (Enumeration<? extends ZipEntry> entries = jarArchive.entries(); entries.hasMoreElements(); ) {
				ZipEntry e = entries.nextElement();
				int nRead;
				if (e.getName().endsWith(".class")) {
					InputStream is = jarArchive.getInputStream(e);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					while((nRead = is.read(data, 0, data.length)) != -1)
						baos.write(data, 0, nRead);
					baos.flush();
					addClass(baos.toByteArray());
				}
			}
		}
	}

	/**
	 * Add classes from the given jar to the inheritance graph.
	 *
	 * @param clazz
	 * 		Class file to use.
	 *
	 * @throws IOException
	 * 		when the class file cannot be read.
	 */
	public void addClass(File clazz) throws IOException {
		addClass(Files.readAllBytes(clazz.toPath()));
	}

	/**
	 * Add classes from the given jar to the inheritance graph.
	 *
	 * @param code
	 * 		Class bytecode.
	 */
	public void addClass(byte[] code) {
		Set<String> parents = new HashSet<>();
		ClassReader cr = new ClassReader(code);
		String child = cr.getClassName();
		parents.add(cr.getSuperName());
		parents.addAll(Arrays.asList(cr.getInterfaces()));
		add(child, parents);
	}

	/**
	 * Add a child and its parents to the inheritance graph.
	 *
	 * @param child
	 * 		A child type.
	 * @param parents
	 * 		Set of parents of the child.
	 */
	public void add(String child, Set<String> parents) {
		if (child == null || parents == null)
			return;
		parents.remove(null);
		parentsOf.put(child, parents);
		parents.forEach(parent -> childrenOf.putSingle(parent, child));
	}

	/**
	 * @param name
	 * 		Internal name of class.
	 *
	 * @return Direct parents of the class.
	 */
	public Set<String> getParents(String name) {
		Set<String> set = parentsOf.get(name);
		if (set == null)
			return Collections.emptySet();
		return set;
	}

	/**
	 * @param name
	 * 		Internal name of class.
	 *
	 * @return All parents of the class.
	 */
	public Set<String> getAllParents(String name) {
		Set<String> set = parentsOfCachedAll.get(name);
		if (set == null)
			parentsOfCachedAll.put(name, set =
                    (getParents(name).stream()
							.map(n -> getAllParents(n).stream())
							.reduce(getParents(name).stream(), Stream::concat))
							.collect(Collectors.toSet()));
		return set;
	}

	/**
	 * @param name
	 * 		Internal name of class.
	 *
	 * @return Direct children of the class.
	 */
	public Set<String> getChildren(String name) {
		Set<String> set = childrenOf.get(name);
		if (set == null)
			return Collections.emptySet();
		return set;
	}

	/**
	 * @param name
	 * 		Internal name of class.
	 *
	 * @return All children of the class.
	 */
	public Set<String> getAllChildren(String name) {
		Set<String> set = childrenOfCachedAll.get(name);
		if (set == null)
			childrenOfCachedAll.put(name, set =
                    (getChildren(name).stream()
							.map(n -> getAllChildren(n).stream())
							.reduce(getChildren(name).stream(), Stream::concat))
							.collect(Collectors.toSet()));
		return set;
	}

	/**
	 * @return String to write to file for caching purposes.
	 */
	public String convertToString() {
		StringBuilder sb = new StringBuilder();
		childrenOf.forEach((parent, children) -> {
			if (parent.equals("java/lang/object"))
				return;
			sb.append(parent).append(MAP_KV_SPLIT).append(String.join(MAP_VAL_SPLIT, children)).append('\n');
		});
		return sb.toString();
	}
}
