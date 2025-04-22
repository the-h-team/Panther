package com.github.sanctum.panther.util;

import com.github.sanctum.panther.annotation.Experimental;
import com.github.sanctum.panther.container.ImmutablePantherMap;
import com.github.sanctum.panther.container.PantherCollection;
import com.github.sanctum.panther.container.PantherEntryMap;
import com.github.sanctum.panther.container.PantherList;
import com.github.sanctum.panther.container.PantherMap;
import com.google.common.reflect.TypeToken;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A class dedicated to allowing developers to load external jars into memory.
 *
 * @param <T> The optional main class this loader needs to locate and instantiate.
 */
public abstract class AbstractClassLoader<T> extends URLClassLoader {

	protected final PantherMap<String, Class<?>> classes;
	protected final T mainClass;
	protected ClassLoader bukkitHandler;

	protected AbstractClassLoader(@NotNull File file, ClassLoader parent, Object... args) throws IOException {
		this(file, null, parent, args);
	}

	protected AbstractClassLoader(@NotNull File file, @Nullable("Plugin class/instance or classloader.") Object bukkit, ClassLoader parent, Object... args) throws IOException {
		super(new URL[]{file.toURI().toURL()}, parent);
		// TODO: Look to replace guava usage with different library (maybe gson's TypeToken?)
		final Class<T> main = (Class<T>) new TypeToken<T>(getClass()){}.getRawType();
		final Logger logger = PantherLogger.getInstance().getLogger();
		final PantherMap<String, Class<?>> loadedClasses = new PantherEntryMap<>();
		if (!file.isFile()) throw new IllegalArgumentException("The provided file is not a jar file!");
		if (bukkit != null) setBukkitHandler(bukkit);
		new JarFile(file).stream()
				.map(ZipEntry::getName)
				.filter(entryName -> entryName.contains(".class") && !entryName.contains("$"))
				.map(classPath -> classPath.replace('/', '.'))
				.map(className -> className.substring(0, className.length() - 6))
				.forEach(s -> {
					final Class<?> resolvedClass;
					try {
						resolvedClass = loadClass(s, true);
					} catch (ClassNotFoundException e) {
						logger.warning(() -> "Unable to inject '" + s + "'");
						logger.warning(e::getMessage);
						return;
					}
					logger.finest(() -> "Loaded '" + s + "' successfully.");
					if (bukkit != null) {
						getBukkitClassMap().put(s, resolvedClass);
					}
					loadedClasses.put(s, resolvedClass);
				});
		this.classes = loadedClasses;
		if (!Check.isNull(main) && !main.equals(Object.class)) {
			try {
				Class<? extends T> addonClass = loadedClasses.values().stream().filter(main::isAssignableFrom).findFirst().map(aClass -> (Class<? extends T>) aClass).get();
				if (args != null && args.length > 0) {
					Constructor<T> constructor = null;
					for (Constructor<?> con : main.getConstructors()) {
						if (args.length == con.getParameters().length) {
							int success = 0;
							for (int i = 0; i < args.length; i++) {
								Class<?> objectClass = args[i].getClass();
								Class<?> typeClass = con.getParameters()[i].getType();
								if (objectClass.isAssignableFrom(typeClass)) {
									success++;
								}
								if (success == args.length) {
									constructor = (Constructor<T>) con;
									break;
								}
							}
						}
					}
					this.mainClass = constructor != null ? addonClass.getDeclaredConstructor(constructor.getParameterTypes()).newInstance(args) : addonClass.getDeclaredConstructor().newInstance();
				} else {
					this.mainClass = addonClass.getDeclaredConstructor().newInstance();
				}
			} catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
				throw new IllegalStateException("Constructor not public", ex);
			} catch (InstantiationException ex) {
				throw new IllegalStateException("Constructor parameter mismatch", ex);
			}
		} else this.mainClass = null;
	}

	// Set the class loader of a bukkit object FIXME: move whole method to laby?
	void setBukkitHandler(@NotNull Object handler) {
		if (handler instanceof ClassLoader) {
			this.bukkitHandler = (ClassLoader) handler;
		} else {
			if (handler instanceof Class) {
				this.bukkitHandler = ((Class<?>)handler).getClassLoader();
			} else {
				this.bukkitHandler = handler.getClass().getClassLoader();
			}
		}
	}

	/**
	 * Get the main class for this class loader.
	 *
	 * @return the main class for this class loader if one exists.
	 */
	public T getMainClass() {
		return mainClass;
	}

	/**
	 * Get a list of all classes loaded by this class loader.
	 *
	 * @return all classes loaded by this class loader.
	 */
	public PantherCollection<Class<?>> getClasses() {
		return new PantherList<>(classes.values());
	}

	/**
	 * Get the map of loaded classes by this loader.
	 *
	 * @return all classes loaded by this class loader.
	 */
	public PantherMap<String, Class<?>> getClassMap() {
		return ImmutablePantherMap.of(classes);
	}

	/**
	 * Get a resource lookup for this class loader.
	 *
	 * @return a resource lookup object
	 */
	public ResourceLookup getLookup() {
		return new ResourceLookup(this);
	}

	/**
	 * Get a resource lookup for this class loader.
	 *
	 * @param packageName the package name to check
	 * @return a resource lookup object
	 */
	public ResourceLookup getLookup(@NotNull String packageName) {
		return new ResourceLookup(this, packageName);
	}

	/**
	 * Unload a class from memory. If the provided class is not found an exception will occur, if the provided string results in a path
	 * this method will switch in an attempt at locating and removing the relative class files it belongs to.
	 *
	 * @param name The name of the class file or path.
	 * @return true if the class(es) got removed from memory.
	 * @throws ClassNotFoundException if the attempted class resolve fails and the included text doesn't result in a valid directory.
	 */
	@Experimental
	public boolean unload(String name) throws ClassNotFoundException {
		if (classes.containsKey(name)) {
			classes.remove(name);
			if (bukkitHandler != null) {
				getBukkitClassMap().remove(name);
			}
			return true;
		} else throw new ClassNotFoundException("Class " + name + " not found, cannot unload.");
	}

	/**
	 * Simply unload a loaded class from this addon loader.
	 *
	 * @param clazz The class to unload.
	 * @throws WrongLoaderUsedException when the class attempting removal belongs to a different loader instance.
	 * @return true if the class was able to unload.
	 */
	@Experimental
	public boolean unload(Class<?> clazz) throws WrongLoaderUsedException {
		final String name = clazz.getName().replace("/", ".").substring(0, clazz.getName().length() - 6);
		if (!this.classes.containsKey(name)) throw new WrongLoaderUsedException("Class " + clazz.getName() + " does not belong to this loader!");
		this.classes.remove(name);
		if (bukkitHandler != null) {
			getBukkitClassMap().remove(name);
		}
		return true;
	}

	// A reflection getter for internally supporting bukkit. Yes its this easy. FIXME move whole method to laby?
	Map<String, Class<?>> getBukkitClassMap() throws IllegalStateException {
		try {
			Field f = Class.forName("org.bukkit.plugin.java.PluginClassLoader").getDeclaredField("classes");
			f.setAccessible(true);
			return (Map<String, Class<?>>) f.get(this.bukkitHandler);
		} catch (ClassCastException | IllegalAccessException | ClassNotFoundException | NoSuchFieldException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String toString() {
		return "AbstractClassLoader{" +
				"classes=" + classes +
				", mainClass=" + mainClass +
				", bukkitHandler=" + (bukkitHandler != null) +
				'}';
	}
}
