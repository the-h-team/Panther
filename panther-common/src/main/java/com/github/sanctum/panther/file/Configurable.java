package com.github.sanctum.panther.file;

import com.github.sanctum.panther.annotation.AnnotationDiscovery;
import com.github.sanctum.panther.container.PantherCollection;
import com.github.sanctum.panther.container.PantherEntryMap;
import com.github.sanctum.panther.container.PantherMap;
import com.github.sanctum.panther.util.PantherLogger;
import com.github.sanctum.panther.util.MapDecompression;
import com.github.sanctum.panther.util.OrdinalProcedure;
import com.github.sanctum.panther.util.SimpleAsynchronousTask;
import com.github.sanctum.panther.util.TypeAdapter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

/**
 * A utility reserved for delegating {@link Type#JSON} or other types of files.
 *
 * <p>Use this environment to manipulate data sent to / read from a particular file location.</p>
 *
 * @author Hempfest
 * @version 1.0
 */
public abstract class Configurable implements MemorySpace, Root {

	protected static final Map<String, JsonAdapterInput<?>> serializers = new HashMap<>();
	protected final Map<String, MemorySpace> memory = new HashMap<>();
	protected final PantherMap<Class<?>, Generic> processors = new PantherEntryMap<>();

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
	 * @return The desired Json element adapter or null if non existent.
	 */
	public static <V> JsonAdapter<V> getAdapter(@NotNull Class<V> type) {
		return serializers.entrySet().stream().filter(e -> e.getKey().equals(type.getName()) || type.isAssignableFrom(e.getValue().getSerializationSignature())).map(Map.Entry::getValue).map(c -> (JsonAdapter<V>) c).findFirst().orElse(null);
	}

	public static <V> JsonAdapter<V> getAdapter(@NotNull String pointer) {
		return serializers.values().stream().filter(jsonAdapterInput -> pointer.equals(OrdinalProcedure.select(jsonAdapterInput, 24).cast(TypeAdapter.STRING))).map(c -> (JsonAdapter<V>) c).findFirst().orElse(null);
	}

	/**
	 * @param processor
	 */
	public final void register(@NotNull Generic processor) {
		this.processors.put(processor.getClass(), processor);
	}

	/**
	 * @param processor
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

	/**
	 * Delegates file type information in regard to either Json or Yaml formats.
	 *
	 * @author Hempfest
	 * @version 1.0
	 */
	public enum Type implements Extension {
		/**
		 * A file responsible for things like user data/ data of any type.
		 */
		JSON(JsonConfiguration.class),
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

	/**
	 * A configurable node implementation.
	 *
	 * @author Hempfest
	 * @version 1.0
	 */
	public static class Node implements com.github.sanctum.panther.file.Node, Primitive {

		protected final Configurable config;
		protected final String key;

		public Node(String key, Configurable configuration) {
			this.config = configuration;
			this.key = key;
		}

		@Override
		public boolean isNode(String key) {
			return config.isNode(this.key + "." + key);
		}

		@Override
		public com.github.sanctum.panther.file.Node getNode(String node) {
			return (com.github.sanctum.panther.file.Node) Optional.ofNullable(config.memory.get(this.key + "." + node)).orElseGet(() -> {
				Node n = new Node(key + "." + node, config);
				config.memory.put(n.getPath(), n);
				return n;
			});
		}

		@Override
		public Object get() {
			return config.get(key);
		}

		@Override
		public Primitive toPrimitive() {
			return this;
		}

		@Override
		public <T extends Generic> T toGeneric(@NotNull Class<T> clazz) {
			T gen = (T) config.processors.get(clazz);
			if (gen != null) {
				OrdinalProcedure.of(gen).get(20, this).get();
				return gen;
			}
			throw new NullPointerException(clazz + " does not have a registered instance within this configurable!");
		}

		@Override
		public String getString() {
			return config.getString(this.key);
		}

		@Override
		public int getInt() {
			return config.getInt(this.key);
		}

		@Override
		public boolean getBoolean() {
			return config.getBoolean(this.key);
		}

		@Override
		public double getDouble() {
			return config.getDouble(this.key);
		}

		@Override
		public float getFloat() {
			return config.getFloat(this.key);
		}

		@Override
		public long getLong() {
			return config.getLong(this.key);
		}

		@Override
		public List<?> getList() {
			return config.getList(this.key);
		}

		@Override
		public Map<?, ?> getMap() {
			return config.getMap(this.key);
		}

		@Override
		public List<String> getStringList() {
			return config.getStringList(this.key);
		}

		@Override
		public List<Integer> getIntegerList() {
			return config.getIntegerList(this.key);
		}

		@Override
		public List<Double> getDoubleList() {
			return config.getDoubleList(this.key);
		}

		@Override
		public List<Float> getFloatList() {
			return config.getFloatList(this.key);
		}

		@Override
		public List<Long> getLongList() {
			return config.getLongList(this.key);
		}

		@Override
		public boolean isString() {
			return config.isString(this.key);
		}

		@Override
		public boolean isBoolean() {
			return config.isBoolean(this.key);
		}

		@Override
		public boolean isInt() {
			return config.isInt(this.key);
		}

		@Override
		public boolean isDouble() {
			return config.isDouble(this.key);
		}

		@Override
		public boolean isFloat() {
			return config.isFloat(this.key);
		}

		@Override
		public boolean isLong() {
			return config.isLong(this.key);
		}

		@Override
		public boolean isList() {
			return config.isList(this.key);
		}

		@Override
		public boolean isStringList() {
			return config.isStringList(this.key);
		}

		@Override
		public boolean isFloatList() {
			return config.isFloatList(this.key);
		}

		@Override
		public boolean isDoubleList() {
			return config.isDoubleList(this.key);
		}

		@Override
		public boolean isIntegerList() {
			return config.isIntegerList(this.key);
		}

		@Override
		public boolean isLongList() {
			return config.isLongList(this.key);
		}

		@Override
		public <T> T get(Class<T> type) {
			Object o = config.get(this.key, type);
			if (o != null) {
				return (T) o;
			}
			return null;
		}

		@Override
		public String getPath() {
			return this.key;
		}

		@Override
		public boolean delete() {
			if (config.isNode(this.key)) {
				config.set(this.key, null);
				SimpleAsynchronousTask.runNow(() -> config.memory.remove(this.key));
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void reload() {
			config.reload();
		}

		@Override
		public boolean create() {
			if (!config.exists()) {
				try {
					config.create();
				} catch (IOException ex) {
					PantherLogger.getInstance().getLogger().severe("- An issue occurred while attempting to create the backing file for the '" + config.getName() + "' configuration.");
					ex.printStackTrace();
				}
			}
			if (config.getType() == Type.JSON) {
				set(new Object());
			}
			save();
			return false;
		}

		@Override
		public boolean exists() {
			return isNode(this.key) || get() != null;
		}

		@Override
		public boolean save() {
			return config.save();
		}

		@Override
		public void set(Object o) {
			config.set(this.key, o);
		}

		@Override
		public com.github.sanctum.panther.file.Node getParent() {
			String[] k = this.key.split("//.");
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < k.length - 1; i++) {
				builder.append(k[i]).append(".");
			}
			String key = builder.toString();
			if (key.endsWith(".")) {
				key = key.substring(0, builder.length() - 1);
			}
			if (key.equals(this.key)) return this;
			return getNode(key);
		}

		@Override
		public String toJson() {
			return JsonAdapter.getJsonBuilder().setPrettyPrinting().enableComplexMapKeySerialization().serializeNulls().setLenient().serializeSpecialFloatingPointValues().create().toJson(get());
		}

		@Override
		public Set<String> getKeys(boolean deep) {
			Set<String> keys = new HashSet<>();
			if (config.get(this.key) instanceof Map) {
				Map<String, Object> level1 = (Map<String, Object>) config.get(this.key);
				if (deep) {
					return MapDecompression.getInstance().decompress(level1.entrySet(), '.', null).toSet();
				} else {
					keys.addAll(level1.keySet());
				}
			} else {
				keys.add(this.key);
			}
			return keys;
		}

		@Override
		public Map<String, Object> getValues(boolean deep) {
			Map<String, Object> map = new HashMap<>();
			if (config.get(this.key) instanceof Map) {
				Map<String, Object> level1 = (Map<String, Object>) config.get(this.key);
				if (deep) {
					return MapDecompression.getInstance().decompress(level1.entrySet(), '.', null).toMap();
				} else {
					map.putAll(level1);
				}
			} else {
				map.put(this.key, get());
			}
			return map;
		}

	}
}
