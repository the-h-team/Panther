package com.github.sanctum.panther.file;

import com.github.sanctum.panther.annotation.AnnotationDiscovery;
import com.github.sanctum.panther.annotation.Note;
import com.github.sanctum.panther.container.PantherCollection;
import com.github.sanctum.panther.container.PantherCollectors;
import com.github.sanctum.panther.container.PantherEntryMap;
import com.github.sanctum.panther.container.PantherList;
import com.github.sanctum.panther.container.PantherMap;
import com.github.sanctum.panther.file.handler.EditorHandle;
import com.github.sanctum.panther.file.handler.NonExistentParentException;
import com.github.sanctum.panther.util.MapDecompression;
import com.github.sanctum.panther.util.OrdinalProcedure;
import com.github.sanctum.panther.util.PantherLogger;
import com.github.sanctum.panther.util.SimpleAsynchronousTask;
import com.github.sanctum.panther.util.TypeAdapter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// TODO document base api, pull impl off into separate class(es)
/**
 * A utility reserved for delegating {@link Type#JSON} or other types of files.
 *
 * <p>Use this environment to manipulate data sent to / read from a particular file location.</p>
 *
 * @since 1.0.2
 * @author Hempfest
 * @version 2.0
 */
public abstract class Configurable implements MemorySpace, Root {

	protected static final Map<String, JsonAdapterInput<?>> serializers = new HashMap<>();
	protected static final PantherCollection<Handle> handlers = new PantherList<>();
	protected final Map<String, MemorySpace> memory = new HashMap<>();
	protected final PantherMap<Class<?>, Generic> processors = new PantherEntryMap<>();

	/**
	 * @param processor an element used for internal object parsing.
	 */
	public final void register(@NotNull Generic processor) {
		this.processors.put(processor.getClass(), processor);
	}

	/**
	 * @param processor an element used for internal object parsing.
	 */
	public final void unregister(@NotNull Generic processor) {
		this.processors.remove(processor.getClass());
	}

	/**
	 * @param key
	 * @return
	 */
	public abstract Object get(String key);

	/**
	 * @param key
	 * @param type
	 * @param <T>
	 * @return
	 */
	public abstract <T> T get(String key, Class<T> type);

	/**
	 * Store an object under a specified path. Any current relative information to the path will be over-written.
	 *
	 * @param key The path to save the object under.
	 * @param o   The object to store.
	 */
	public abstract void set(String key, Object o);

	/**
	 * Get a string.
	 *
	 * @param key The path the string resides under.
	 * @return The string or null.
	 */
	public abstract String getString(String key);

	/**
	 * Get a boolean.
	 *
	 * @param key The path the boolean resides under.
	 * @return The boolean or false
	 */
	public abstract boolean getBoolean(String key);

	/**
	 * Get a double.
	 *
	 * @param key The path the double resides under.
	 * @return The string or 0.0.
	 */
	public abstract double getDouble(String key);

	/**
	 * Get a long.
	 *
	 * @param key The path the long resides under.
	 * @return The long or 0L
	 */
	public abstract long getLong(String key);

	/**
	 * Get a float.
	 *
	 * @param key The path the float resides under.
	 * @return The float or 0.0f
	 */
	public abstract float getFloat(String key);

	/**
	 * Get an integer.
	 *
	 * @param key The path the integer resides under.
	 * @return The integer or 0
	 */
	public abstract int getInt(String key);

	/**
	 * Get a map.
	 *
	 * @param key The path the map resides under.
	 * @return The map or a new empty one.
	 */
	public abstract Map<?, ?> getMap(String key);

	/**
	 * Get a list.
	 *
	 * @param key The path the list resides under.
	 * @return The list or a new empty one.
	 */
	public abstract List<?> getList(String key);

	/**
	 * Get a string list.
	 *
	 * @param key The path the string list resides under.
	 * @return The string list or a new empty one.
	 */
	public abstract List<String> getStringList(String key);

	/**
	 * Get an integer list.
	 *
	 * @param key The path the integer list resides under.
	 * @return The integer list or a new empty one.
	 */
	public abstract List<Integer> getIntegerList(String key);

	/**
	 * Get a double list.
	 *
	 * @param key The path the double list resides under.
	 * @return The double list or a new empty one.
	 */
	public abstract List<Double> getDoubleList(String key);

	/**
	 * Get a float list.
	 *
	 * @param key The path the float list resides under.
	 * @return The float list or a new empty one.
	 */
	public abstract List<Float> getFloatList(String key);

	/**
	 * Get a long list.
	 *
	 * @param key The path the long list resides under.
	 * @return The long list or a new empty one.
	 */
	public abstract List<Long> getLongList(String key);

	/**
	 * Check if a list resides within a specified path.
	 *
	 * @param key The path to check.
	 * @return true if a list was found.
	 */
	public abstract boolean isList(String key);

	/**
	 * Check if a string list resides within a specified path.
	 *
	 * @param key The path to check.
	 * @return true if a string list was found.
	 */
	public abstract boolean isStringList(String key);

	/**
	 * Check if a float list resides within a specified path.
	 *
	 * @param key The path to check.
	 * @return true if a float list was found.
	 */
	public abstract boolean isFloatList(String key);

	/**
	 * Check if a double list resides within a specified path.
	 *
	 * @param key The path to check.
	 * @return true if a double list was found.
	 */
	public abstract boolean isDoubleList(String key);

	/**
	 * Check if a long list resides within a specified path.
	 *
	 * @param key The path to check.
	 * @return true if a long list was found.
	 */
	public abstract boolean isLongList(String key);

	/**
	 * Check if an integer list resides within a specified path.
	 *
	 * @param key The path to check.
	 * @return true if an integer list was found.
	 */
	public abstract boolean isIntegerList(String key);

	/**
	 * Check if a boolean resides within a specified path.
	 *
	 * @param key The path to check.
	 * @return true if a boolean was found.
	 */
	public abstract boolean isBoolean(String key);

	/**
	 * Check if a double resides within a specified path.
	 *
	 * @param key The path to check.
	 * @return true if a double was found.
	 */
	public abstract boolean isDouble(String key);

	/**
	 * Check if an int resides within a specified path.
	 *
	 * @param key The path to check.
	 * @return true if an integer was found.
	 */
	public abstract boolean isInt(String key);

	/**
	 * Check if a long resides within a specified path.
	 *
	 * @param key The path to check.
	 * @return true if a long was found.
	 */
	public abstract boolean isLong(String key);

	/**
	 * Check if a float resides within a specified path.
	 *
	 * @param key The path to check.
	 * @return true if a float was found.
	 */
	public abstract boolean isFloat(String key);

	/**
	 * Check if a string resides within a specified path.
	 *
	 * @param key The path to check.
	 * @return true if a string was found.
	 */
	public abstract boolean isString(String key);

	/**
	 * Get the name of this Config.
	 *
	 * @return name of Config
	 */
	public abstract String getName();

	/**
	 * Get the description of the config if it has one.
	 * <p>
	 * Used to resolve subdirectory if present.
	 *
	 * @return this config's sub-directory
	 */
	public abstract String getDirectory();

	/**
	 * Get the backing file for this Config.
	 * <p>
	 * A mandatory {@link Configurable#exists()} check should also be used before
	 * accessing a file directly following the {@link Configurable#create()} method.
	 *
	 * @return backing file File object
	 */
	public abstract File getParent();

	/**
	 * @return The type this file represents.
	 */
	public Extension getType() {
		return Type.UNKNOWN;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj instanceof Configurable) {
			Configurable c = (Configurable) obj;
			return Objects.equals(getName(), c.getName()) &&
					Objects.equals(getDirectory(), c.getDirectory())
					&& getType() == c.getType();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getName(), getDirectory(), getType().toString());
	}

	public abstract static class Handle {

		public @NotNull String getName() {
			return getClass().getSimpleName();
		}

	}

	/**
	 * Delegates file type information in regard to either Json or Yaml formats.
	 *
	 * @author Hempfest
	 * @version 1.0
	 */
	public enum Type implements Extension {
		/**
		 * A code block orientated key-value style data type. (Doesn't accept comments)
		 */
		JSON(AbstractJsonConfiguration.class),
		/**
		 * A table of contents key-value style data type. (Accepts comments)
		 */
		YAML(AbstractYamlConfiguration.class),
		/**
		 * An un-specified implementation of configurable.
		 */
		UNKNOWN(Configurable.class);

		private final Class<? extends Configurable> c;

		Type(Class<? extends Configurable> cl) {
			this.c = cl;
		}

		@Override
		public String get() {
			switch (this) {
				case JSON:
					return ".json";
				case YAML:
					return ".yml";
				case UNKNOWN:
				default:
					throw new IllegalArgumentException("Unknown file extension!");
			}
		}

		@Override
		public Class<? extends Configurable> getImplementation() {
			return c;
		}
	}

	/**
	 * Information on a file type like ".json" or ".yml" and a link to an implementation.
	 *
	 * @author Hempfest
	 * @version 1.0
	 */
	public interface Extension {

		/**
		 * @return the file type extension.
		 */
		String get();

		/**
		 * Attempts to get the super class type.
		 *
		 * <p>If the type represents a custom implementation then the base Configurable class is returned.</p>
		 *
		 * @return The possible super class for this type.
		 */
		default Class<? extends Configurable> getImplementation() {
			return Configurable.class;
		}

	}

	public interface Host {

		default Editor getFile(@NotNull String name, @Nullable String description, @NotNull Configurable.Extension extension) {
			return view(this).get(name, description, extension);
		}

		@NotNull String getName();

		@NotNull File getDataFolder();

	}

	/**
	 * A configurable node implementation.
	 *
	 * @author Hempfest
	 * @version 1.0
	 */
	public static class Node extends ConfigurableNodeImpl {
		public Node(String key, Configurable configuration) {
			super(key, configuration);
		}
	}

	/**
	 * Encapsulates file operations for things such as modification/retrieval
	 */
	public static class Editor {

		protected final Configurable configuration;
		protected final Host host;

		protected Editor(@NotNull Configurable.Host host, @NotNull Configurable configuration) {
			this.host = host;
			this.configuration = configuration;
		}

		protected Editor(@NotNull Configurable.Host host, @NotNull final String n, @Nullable final String d, Extension data) {
			PantherList<EditorHandle> handles = getHandles(EditorHandle.class);
			Configurable configurable = null;
			for (EditorHandle handle : handles) {
				if (handle != null) {
					// check our file extension, if applicable use impl
					if (data.get().endsWith("data") || data.get().endsWith("json") || data.get().endsWith("yml")) {
						configurable = handle.onInstantiate(host, n, d, data);
					}
				}
			}
			if (configurable != null) {
				this.host = host;
				this.configuration = configurable;
			} else
				throw new IllegalStateException("Unable to locate the Editor Handle and therefore cannot parse file creation.");
		}

		/**
		 * Get the configurable root for this file manager.
		 *
		 * @return The configurable root for this manager.
		 */
		public Configurable getRoot() {
			return configuration;
		}

		/**
		 * Performs operations on this FileManager's config instance,
		 * returning an object of any desired type. Accepts a lambda,
		 * allowing for clean and compile-time-type-safe data retrieval and
		 * mapping.
		 *
		 * @param fun an operation returning an object of arbitrary type
		 *            {@link R} from the configuration
		 * @param <R> type of the returned object (inferred)
		 * @return the value produced by the provided function
		 */
		public <R> R read(Function<Configurable, R> fun) {
			return fun.apply(configuration);
		}

		/**
		 * A functional delegation to data table consumption
		 *
		 * @param table The data consumption to take place.
		 * @see Editor#write(DataTable)
		 */
		public void write(Consumer<? super DataTable> table) {
			DataTable t = DataTable.newTable();
			table.accept(t);
			write(t);
		}

		/**
		 * Set/Replace & save multiple keyed value spaces within this file.
		 *
		 * @param table The data table to use when setting values.
		 * @see Editor#write(DataTable, boolean)
		 */
		@Note("You can create a fresh DataTable really easily see DataTable#newTable()")
		public void write(@Note("Provided table gets cleared upon finalization.") DataTable table) {
			write(table, true);
		}

		/**
		 * Set & save multiple keyed value spaces within this file.
		 *
		 * <p>After all inquires have been transferred the inquiry object is cleared and discarded due to
		 * being of no further importance.</p>
		 *
		 * <p>Takes all 2d declarations and forms them into multi-layered nodes.</p>
		 *
		 * <p>By default this method is set to override any already existing nodes store
		 * within the configurable</p>
		 *
		 * @param replace Whether to replace already set values from file with ones from the table
		 * @see Editor#write(DataTable)
		 */
		@Note("You can create a fresh DataTable really easily see DataTable#newTable()")
		public void write(@Note("Provided table gets cleared upon finalization.") DataTable table, boolean replace) {
			PantherList<EditorHandle> handles = getHandles(EditorHandle.class);
			for (EditorHandle handle : handles) {
				if (handle != null) {
					for (Map.Entry<String, Object> entry : table.values().entrySet()) {
						Object o = handle.onWriteFromTable(entry.getValue(), entry.getKey(), configuration, replace);
						if (o != null) {
							if (o == DataTable.NULL) {
								configuration.set(entry.getKey(), null);
							} else configuration.set(entry.getKey(), o);
						}
					}
				}
				// instantly clear up space (help GC, we don't need these elements anymore.)
				table.clear();
				configuration.save();
			}
		}

		/**
		 * Copy all values from this yml file to a json file of similar stature.
		 *
		 * @param name The new name of the file.
		 * @param dir  The optional new directory, null places in base folder.
		 * @return a new json file containing all values from this yml file.
		 */
		public @NotNull Configurable.Editor toJSON(@NotNull String name, String dir) {
			Editor n = view(host).get(name, dir, Type.JSON);
			Configurable c = getRoot();
			if (c instanceof AbstractYamlConfiguration) {
				n.write(copy(), false);
				return n;
			}
			return this;
		}

		/**
		 * Copy all values from this json file to a yml file of similar stature.
		 *
		 * @param name The new name of the file.
		 * @param dir  The optional new directory, null places in base folder.
		 * @return a new yml file containing all values from this json file.
		 */
		public @NotNull Configurable.Editor toYaml(@NotNull String name, String dir) {
			Editor n = view(host).get(name, dir, Type.YAML);
			Configurable c = getRoot();
			if (c instanceof AbstractJsonConfiguration) {
				n.write(copy(), false);
				return n;
			}
			return this;
		}

		/**
		 * Copy all values from this yml file to a json file of similar stature.
		 *
		 * @return a new json file containing all values from this yml file.
		 */
		public @NotNull Configurable.Editor toJSON() {
			return toJSON(getRoot().getName(), getRoot().getDirectory());
		}

		/**
		 * Copy all values from this json file to a yml file of similar stature.
		 *
		 * @return a new yml file containing all values from this json file.
		 */
		public @NotNull Configurable.Editor toYaml() {
			return toYaml(getRoot().getName(), getRoot().getDirectory());
		}

		/**
		 * Move this file to another location. Retains all values but doesn't retain comments, only headers.
		 * *Automatically deletes old file when moved*
		 *
		 * @param dir The optional new directory to move to, null places in base folder.
		 * @return a new file containing all the values from this file.
		 */
		public @NotNull Configurable.Editor toMoved(String dir) {
			// gotta love our api sometimes, just look at how clean it is to copy ALL values from a config to another location.
			final Editor n = view(host).get(getRoot().getName(), dir, getRoot().getType());
			Configurable c = getRoot();
			n.write(copy(), false);
			c.delete();
			return n;
		}

		/**
		 * Copy all contents to a datatable.
		 *
		 * @return a fresh datatable containing all values from this file.
		 */
		public @NotNull DataTable copy() {
			Configurable c = getRoot();
			DataTable inquiry = DataTable.newTable();
			c.getValues(true).forEach(inquiry::set);
			return inquiry;
		}

		public @NotNull Editor reset() throws NonExistentParentException {
			PantherList<EditorHandle> handles = getHandles(EditorHandle.class);
			for (EditorHandle handle : handles) {
				if (handle != null) {
					InputStream stream = handle.onReset(this.host, configuration.getName() + configuration.getType().get(), null);
					if (stream != null) {
						copy(stream, configuration.getParent());
						getRoot().reload();
						getRoot().save();
					} else
						throw new NonExistentParentException("There is no file by the name of " + configuration.getName() + " in this applications resources.");
				}
			}
			return this;
		}

		public @NotNull Editor reset(@NotNull String fileName) throws NonExistentParentException {
			PantherList<EditorHandle> handles = getHandles(EditorHandle.class);
			for (EditorHandle handle : handles) {
				if (handle != null) {
					InputStream stream = handle.onReset(this.host, configuration.getName(), fileName);
					if (stream != null) {
						copy(stream, configuration.getParent());
						getRoot().reload();
						getRoot().save();
					} else
						throw new NonExistentParentException("There is no file by the name of " + configuration.getName() + " in this applications resources.");
				}
			}
			return this;
		}

		public @NotNull Editor resetYaml(@NotNull String yamlName) throws NonExistentParentException {
			PantherList<EditorHandle> handles = getHandles(EditorHandle.class);
			for (EditorHandle handle : handles) {
				if (handle != null) {
					InputStream stream = handle.onReset(this.host, configuration.getName(), yamlName + ".yml");
					if (stream != null) {
						copy(stream, configuration.getParent());
						getRoot().reload();
						getRoot().save();
					} else
						throw new NonExistentParentException("There is no file by the name of " + configuration.getName() + " in this applications resources.");
				}
			}
			return this;
		}

		public @NotNull Editor resetJson(@NotNull String jsonName) throws NonExistentParentException {
			PantherList<EditorHandle> handles = getHandles(EditorHandle.class);
			for (EditorHandle handle : handles) {
				if (handle != null) {
					InputStream stream = handle.onReset(this.host, configuration.getName(), jsonName + ".json");
					if (stream != null) {
						copy(stream, configuration.getParent());
						getRoot().reload();
						getRoot().save();
					} else
						throw new NonExistentParentException("There is no file by the name of " + configuration.getName() + " in this applications resources.");
				}
			}
			return this;
		}

		void copy(InputStream in, File file) {
			try {
				OutputStream out = new FileOutputStream(file);
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				out.close();
				in.close();
			} catch (FileNotFoundException e) {
				throw new IllegalArgumentException("Cannot copy whole directories at a time! (" + file.getPath() + ")", e);
			} catch (IOException e) {
				throw new IllegalStateException("Unable to write to file! See log:", e);
			}
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof Editor)) return false;
			Editor config = (Editor) o;
			return configuration.equals(config.configuration);
		}

		@Override
		public int hashCode() {
			return configuration.hashCode();
		}
	}

	/**
	 * Using a {@link com.github.sanctum.panther.file.Configurable.Host} as the key, look for specific files.
	 * <p>
	 * <strong>Design:</strong> <em>Using your host's main class instance,
	 * create custom data files with ease--sourced direct from your host's
	 * data folder.</em>
	 *
	 * @param host the host source to browse
	 * @return a potential listing of configuration
	 */
	public static ConfigurableEditorQuery view(@NotNull final Configurable.Host host) {
		return ConfigurableEditorQuery.REGISTRY.computeIfAbsent(host.getName(), name -> new ConfigurableEditorQuery(host));
	}

	/**
	 * Register a json element adapter for automatic use with json data usage.
	 *
	 * <p>All element adapters require a {@link com.github.sanctum.panther.file.Node.Pointer} annotation to resolve types when deserializing.
	 * If a pointer is not present this adapter cannot properly register.</p>
	 *
	 * <p>All {@link JsonAdapter} object's require a no parameter accessible constructor through the use of this method. In the event of one missing an exception may occur.</p>
	 *
	 * @param c The adapter to register.
	 * @throws InvalidJsonAdapterException if the provided adapter class doesn't have a valid constructor.
	 */
	public static void registerClass(@NotNull Class<? extends JsonAdapter<?>> c) throws InvalidJsonAdapterException {
		try {
			AnnotationDiscovery<com.github.sanctum.panther.file.Node.Pointer, ? extends JsonAdapter<?>> test = AnnotationDiscovery.of(com.github.sanctum.panther.file.Node.Pointer.class, c);
			if (test.isPresent()) {
				JsonAdapter<?> d = null;
				String alias = test.mapFromClass((r, u) -> r.value());
				if (alias != null && !alias.isEmpty()) {
					Class<? extends JsonAdapter<?>> n = test.mapFromClass((r, u) -> r.type());
					if (n != null) {
						if (!JsonAdapter.Dummy.class.isAssignableFrom(n)) {
							d = n.getDeclaredConstructor().newInstance();
						} else {
							d = c.getDeclaredConstructor().newInstance();
						}
					} else {
						d = c.getDeclaredConstructor().newInstance();
					}
				} else {
					Class<? extends JsonAdapter<?>> n = test.mapFromClass((r, u) -> r.type());
					if (n != null) {
						if (!JsonAdapter.Dummy.class.isAssignableFrom(n)) {
							d = n.getDeclaredConstructor().newInstance();
						} else {
							d = c.getDeclaredConstructor().newInstance();
						}
						alias = c.getSimpleName();
					}
				}
				if (d == null)
					throw new InvalidJsonAdapterException("NodePointer context missing, JSON object serialization requires either an alias or class.");
				serializers.put(alias, new JsonAdapterInput.Impl<>(d));
			} else
				throw new InvalidJsonAdapterException("NodePointer annotation missing, JSON object serialization requires it.");
		} catch (Exception e) {
			PantherLogger.getInstance().getLogger().severe("Class " + c.getSimpleName() + " failed to register JSON serialization handlers.");
			e.printStackTrace();
		}
	}

	/**
	 * Register a json element adapter for automatic use with json data usage.
	 *
	 * <p>All element adapters require a {@link com.github.sanctum.panther.file.Node.Pointer} annotation to resolve types when deserializing.
	 * If a pointer is not present this adapter cannot properly register.</p>
	 *
	 * <p>All {@link JsonAdapter} object's require a no parameter accessible constructor through the use of this method. In the event of one missing an exception may occur.</p>
	 *
	 * @param c       The adapter to register.
	 * @param objects The constructor arguments for adapter instantiation.
	 * @throws InvalidJsonAdapterException if the provided adapter class doesn't have a valid constructor.
	 */
	public static void registerClass(@NotNull Class<? extends JsonAdapter<?>> c, Object... objects) throws InvalidJsonAdapterException {
		try {
			Class<? extends JsonAdapter<?>> d = null;
			AnnotationDiscovery<com.github.sanctum.panther.file.Node.Pointer, ? extends JsonAdapter<?>> test = AnnotationDiscovery.of(com.github.sanctum.panther.file.Node.Pointer.class, c);
			if (test.isPresent()) {
				String alias = test.mapFromClass((r, u) -> r.value());
				if (alias != null && !alias.isEmpty()) {
					Class<? extends JsonAdapter<?>> n = test.mapFromClass((r, u) -> r.type());
					if (n != null) {
						if (!JsonAdapter.Dummy.class.isAssignableFrom(n)) {
							d = n;
						} else {
							d = c;
						}
					} else {
						d = c;
					}
				} else {
					Class<? extends JsonAdapter<?>> n = test.mapFromClass((r, u) -> r.type());
					if (n != null) {
						if (!JsonAdapter.Dummy.class.isAssignableFrom(n)) {
							d = n;
						} else {
							d = c;
						}
						alias = c.getSimpleName();
					}
				}
				if (d == null)
					throw new InvalidJsonAdapterException("NodePointer context missing, JSON object serialization requires either an alias or class.");
				Constructor<?> constructor = null;
				for (Constructor<?> con : d.getConstructors()) {
					if (objects.length == con.getParameters().length) {
						int success = 0;
						for (int i = 0; i < objects.length; i++) {
							Class<?> objectClass = objects[i].getClass();
							Class<?> typeClass = con.getParameters()[i].getType();
							if (objectClass.isAssignableFrom(typeClass)) {
								success++;
							}
							if (success == objects.length) {
								constructor = con;
								break;
							}
						}
					}
				}
				if (constructor != null) {
					serializers.put(alias, new JsonAdapterInput.Impl<>(d.getDeclaredConstructor(constructor.getParameterTypes()).newInstance(objects)));
				} else {
					serializers.put(alias, new JsonAdapterInput.Impl<>(d.getDeclaredConstructor().newInstance()));
				}
			} else
				throw new InvalidJsonAdapterException("NodePointer annotation missing, JSON object serialization requires it.");
		} catch (Exception e) {
			PantherLogger.getInstance().getLogger().severe("Class " + c.getSimpleName() + " failed to register JSON serialization handlers.");
			e.printStackTrace();
		}
	}


	/**
	 * Search for the desired element adapter for quick use.
	 *
	 * @param type The type of adapter to get.
	 * @param <V>  The adapter type.
	 * @return The desired Json element adapter or null if nonexistent.
	 */
	public static <V> JsonAdapter<V> getAdapter(@NotNull Class<V> type) {
		return serializers.entrySet().stream().filter(e -> e.getKey().equals(type.getName()) || type.isAssignableFrom(e.getValue().getSerializationSignature())).map(Map.Entry::getValue).map(c -> (JsonAdapter<V>) c).findFirst().orElse(null);
	}

	public static <V> JsonAdapter<V> getAdapter(@NotNull String pointer) {
		return serializers.values().stream().filter(jsonAdapterInput -> pointer.equals(OrdinalProcedure.select(jsonAdapterInput, 24).cast(TypeAdapter.STRING))).map(c -> (JsonAdapter<V>) c).findFirst().orElse(null);
	}

	public static void addHandle(@NotNull Configurable.Handle handler) {
		handlers.add(handler);
	}

	public static void removeHandle(@NotNull Configurable.Handle handler) {
		handlers.remove(handler);
	}

	public static @Nullable <T extends Configurable.Handle> T getHandle(Class<T> handle) {
		if (handlers.size() == 0) {
			// our handlers are empty, add defaults.
			newComposer();
		}
		PantherList<Configurable.Handle> handles = handlers.stream().filter(h -> handle.isAssignableFrom(h.getClass())).collect(PantherCollectors.toList());
		return (T) handles.getLast();
	}

	public static @NotNull <T extends Configurable.Handle> PantherList<T> getHandles(Class<T> handle) {
		if (handlers.size() == 0) {
			// our handlers are empty, add defaults.
			newComposer();
		}
		return handlers.stream().filter(h -> handle.isAssignableFrom(h.getClass())).map(handle1 -> (T)handle1).collect(PantherCollectors.toList());
	}

	static void newComposer() {
		addHandle(new EditorHandle() {
			@Override
			public @NotNull Configurable onInstantiate(Configurable.@NotNull Host host, @NotNull String name, @Nullable String desc, Configurable.@NotNull Extension extension) {
				if (extension == Configurable.Type.JSON) {
					return new JsonConfiguration(host.getDataFolder(), name, desc);

				} else if (extension == Configurable.Type.YAML) {
					throw new IllegalStateException("The default handler cannot construct yaml file wrappers!");
				}
				throw new IllegalArgumentException("Cannot construct unknown file extension.");
			}

			@Override
			public @Nullable InputStream onReset(Configurable.@NotNull Host host, @NotNull String name, @Nullable String fileName) {
				return null;
			}

			@Override
			public <R> @Nullable R onWriteFromTable(@NotNull R value, @NotNull String key, @NotNull MemorySpace memorySpace, boolean toReplace) {
				if (toReplace) {
					return value;
				} else {
					if (!memorySpace.isNode(key)) { // only setting a value if one isn't there already.
						return value;
					}
				}
				return null;
			}
		});
	}

}
