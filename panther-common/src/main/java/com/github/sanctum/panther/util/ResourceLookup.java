package com.github.sanctum.panther.util;

import com.github.sanctum.panther.container.PantherCollection;
import com.github.sanctum.panther.container.PantherList;
import com.github.sanctum.panther.container.PantherQueue;
import com.github.sanctum.panther.container.PantherSet;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ResourceLookup {

	final ClassLoader loader;
	final PantherQueue<Class<?>> classes;

	public ResourceLookup(@NotNull ClassLoader loader) {
		this.loader = loader;
		this.classes = new PantherQueue<>();
	}

	/**
	 * Queues all classes from a specific base package. FIXME complete doc
	 */
	public ResourceLookup(@NotNull ClassLoader loader, @NotNull String packageName) {
		this(loader);
		getClasses(packageName);
	}

	public @Nullable Class<?> getClass(@NotNull String simpleName, @NotNull String packageName) {
		return getClasses(packageName).stream().filter(c -> c.getSimpleName().equals(simpleName)).findFirst().orElse(null);
	}

	public @Nullable Class<?> getClass(@NotNull String simpleName) {
		return getClasses().get(c -> c.getSimpleName().equals(simpleName));
	}

	public @NotNull PantherQueue<Class<?>> getClasses() {
		return classes;
	}

	public @NotNull PantherCollection<Class<?>> getClasses(@NotNull String packageName) {
		try {
			PantherCollection<Class<?>> collection = getClasses(packageName, loader);
			TaskChain.getAsynchronous().wait(() -> collection.forEach(c -> {
				if (!classes.contains(c)) {
					classes.add(c);
				}
			}), 50);
			return collection;
		} catch (Exception e) {
			return new PantherList<>();
		}
	}

	public @Nullable AbstractJarScanner getScanner(@NotNull String file) {
		InputStream stream = loader.getResourceAsStream(file);
		if (stream != null) return new AbstractJarScanner(stream){};
		return null;
	}

	/**
	 * Scans all classes accessible from the context class loader which belong
	 * to the given package and sub packages. Adapted from
	 * http://snippets.dzone.com/posts/show/4831 and extended to support use of
	 * JAR files
	 *
	 * @param packageName The base package
	 * @param classLoader The class loader to retrieve classes from
	 * @return The classes
	 * @throws Exception if there was an issue getting a class or resource.
	 */
	public static PantherCollection<Class<?>> getClasses(String packageName, @NotNull ClassLoader classLoader) throws Exception {
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(path);
		PantherCollection<String> dirs = new PantherList<>();
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			dirs.add(resource.getFile());
		}
		PantherCollection<String> classes = new PantherSet<>();
		for (String directory : dirs) {
			Iterable<String> iterable = getClassNames(directory, packageName);
			classes.addAll(iterable);
		}
		PantherCollection<Class<?>> classList = new PantherList<>();
		for (String clazz : classes) {
			try {
				Class<?> c = Class.forName(clazz);
				classList.add(c);
			} catch (NoClassDefFoundError ignored) {}
		}
		return classList;
	}

	private static TreeSet<String> getClassNames(String directory, String packageName) throws Exception {
		TreeSet<String> classes = new TreeSet<>();
		if (directory.startsWith("file:") && directory.contains("!")) {
			String[] split = directory.split("!");
			URL jar = new URL(split[0]);
			try (ZipInputStream zip = new ZipInputStream(jar.openStream())) {
				ZipEntry entry = null;
				while ((entry = zip.getNextEntry()) != null) {
					if (entry.getName().endsWith(".class")) {
						String className = entry.getName()
								.replaceAll("[$].*", "")
								.replaceAll("[.]class", "")
								.replace('/', '.');
						if (className.startsWith(packageName)) {
							classes.add(className);
						}
					}
				}
			}
		}
		File dir = new File(directory);
		if (!dir.exists()) {
			return classes;
		}
		File[] files = dir.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					assert !file.getName().contains(".");
					classes.addAll(getClassNames(file.getAbsolutePath(),
							packageName + "." + file.getName()));
				} else if (file.getName().endsWith(".class")) {
					classes.add(packageName
							+ '.'
							+ file.getName().substring(0,
							file.getName().length() - 6));
				}
			}
		}

		return classes;
	}

}
