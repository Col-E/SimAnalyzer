package me.coley.analysis.util;

import org.objectweb.asm.ClassReader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
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
 * @author Matt Coley
 */
public class InheritanceGraph {
	private static final String MAP_KV_SPLIT = ":::";
	private static final String MAP_VAL_SPLIT = ",";
	private final SetMap<String, String> parentsOf = new SetMap<>();
	private final SetMap<String, String> childrenOf = new SetMap<>();
	private final SetMap<String, String> parentsOfCachedAll = new SetMap<>();
	private final SetMap<String, String> childrenOfCachedAll = new SetMap<>();

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
	 * Adds the Java 8 and below 'rt.jar' to the inheritance graph.
	 *
	 * @throws IOException
	 * 		When the RT jar cannot be read.
	 */
	public void addRtJar() throws IOException {
		Path rtJar = Paths.get(System.getProperties().getProperty("java.home"), "lib", "rt.jar");
		if (Files.isRegularFile(rtJar)) {
			addArchive(rtJar.toFile());
		} else {
			throw new IOException("Could not locate 'rt.jar' in 'java.home' from relative path '/lib/rt.jar'");
		}
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
				else if (file.getName().endsWith(".jmod") || file.getName().endsWith(".jmod"))
					addArchive(file);
			}
		}
	}

	/**
	 * Add classes from the current module path to the inheritance graph.
	 * <br>
	 * Intended to be used in a Java 9+ environment.
	 *
	 * @return {@code true} when successfully run.
	 * {@code false} implies the module path could not be read, probably because you are on JDK 8.
	 */
	@SuppressWarnings("unchecked")
	public boolean addModulePath() {
		try {
			// Set<ModuleReference> refs = ModuleFinder.ofSystem().findAll()
			Class<?> c_finder = Class.forName("java.lang.module.ModuleFinder");
			Method finder_ofSystem = c_finder.getDeclaredMethod("ofSystem");
			Method finder_findAll = c_finder.getDeclaredMethod("findAll");
			Object result = finder_ofSystem.invoke(null);
			Set<?> refs = (Set<?>) finder_findAll.invoke(result);
			// For loop contents
			Class<?> c_ref = Class.forName("java.lang.module.ModuleReference");
			Class<?> c_reader = Class.forName("java.lang.module.ModuleReader");
			Method ref_open = c_ref.getDeclaredMethod("open");
			Method reader_list = c_reader.getDeclaredMethod("list");
			Method reader_read = c_reader.getDeclaredMethod("read", String.class);
			Method reader_close = c_reader.getDeclaredMethod("release", ByteBuffer.class);
			for (Object ref : refs) {
				// ModuleReader reader = ref.open();
				// reader.list().filter(name -> name.endsWith(".class"))
				Object reader = ref_open.invoke(ref);
				Stream<String> stream = (Stream<String>) reader_list.invoke(reader);
				stream.filter(name -> name.endsWith(".class")).forEach(name -> {
					try {
						// Optional<ByteBuffer> read = reader.read(name);
						Optional<ByteBuffer> read = (Optional<ByteBuffer>) reader_read.invoke(reader, name);
						if (read.isPresent()) {
							ByteBuffer buffer = read.get();
							byte[] bytecode = new byte[buffer.remaining()];
							buffer.slice().get(bytecode);
							reader_close.invoke(reader, buffer);
							addClass(bytecode);
						}
					} catch (Exception ignored) {
						// no-op
					}
				});
			}
		} catch (Exception ignored) {
			return false;
		}
		return true;
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
		addDirectory(dir.toPath());
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
	public void addDirectory(Path dir) throws IOException {
		if (!Files.isDirectory(dir))
			return;
		Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
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
