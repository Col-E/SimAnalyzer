package me.coley.analysis.util;

import org.objectweb.asm.ClassReader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


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
					while ((nRead = is.read(data, 0, data.length)) != -1)
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
	 * 		Collection of parents of the child.
	 */
	public void add(String child, Collection<String> parents) {
		add(child, new HashSet<>(parents));
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
	 * @return {@code true} when there is a recognized lookup for parents for the given class.
	 * {@code false} when the graph is not aware of any types by the given name.
	 */
	public boolean hasParentLookup(String name) {
		return parentsOf.containsKey(name);
	}

	/**
	 * @param name
	 * 		Internal name of class.
	 *
	 * @return {@code true} when there is a recognized lookup for children for the given class.
	 * {@code false} when the graph is not aware of any types by the given name.
	 */
	public boolean hasChildrenLookup(String name) {
		return childrenOf.containsKey(name);
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
	 * @param first
	 * 		First class name.
	 * @param second
	 * 		Second class name.
	 *
	 * @return Common parent of the classes.
	 */
	public String getCommon(String first, String second) {
		// Full upwards hierarchy for the first
		Set<String> firstParents = getAllParents(first);
		firstParents.add(first);
		// Base case
		if (firstParents.contains(second))
			return second;
		// Iterate over second's parents via breadth-first-search
		Queue<String> queue = new LinkedList<>();
		queue.add(second);
		do {
			// Item to fetch parents of
			String next = queue.poll();
			if (next == null || next.equals("java/lang/Object"))
				break;
			for (String parent : getParents(next)) {
				// Parent in the set of visited classes? Then its valid.
				if (firstParents.contains(parent))
					return parent;
				// Queue up the parent
				if (!parent.equals("java/lang/Object"))
					queue.add(parent);
			}
		} while (!queue.isEmpty());
		// Fallback option
		return "java/lang/Object";
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
