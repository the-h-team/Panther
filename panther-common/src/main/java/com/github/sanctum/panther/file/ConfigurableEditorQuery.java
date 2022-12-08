package com.github.sanctum.panther.file;

import com.github.sanctum.panther.container.PantherEntryMap;
import com.github.sanctum.panther.container.PantherMap;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Encapsulates a plugin for quick and easy file locating/management.
 */
public class ConfigurableEditorQuery {
	// Outer key = plugin name. Inner key = "d;n" where d and n represent the respective fields
	static final PantherMap<String, Map<String, Configurable.Editor>> CACHE = new PantherEntryMap<>();
	static final PantherMap<String, ConfigurableEditorQuery> REGISTRY = new PantherEntryMap<>();

	private final Configurable.Host host;

	ConfigurableEditorQuery(Configurable.Host host) {
		this.host = host;
	}

	/**
	 * Get the host attached to this query.
	 *
	 * @return the host attached to this file search
	 */
	public Configurable.Host getHost() {
		return host;
	}

	/**
	 * For this to be upmost effective it is assumed that you have all configurables cached already.
	 * Example via. {@link ConfigurableEditorQuery#get(String, String)}
	 *
	 * @return a list of all cached configurable editors.
	 */
	public List<Configurable.Editor> gather() {
		return Optional.ofNullable(CACHE.get(host.getName()))
				.map(Map::values)
				.map(ImmutableList::copyOf)
				.orElse(ImmutableList.of());
	}

	/**
	 * Inject a custom implementation of configuration into cache for global use.
	 *
	 * @param configurable The implementation of configurable to inject.
	 */
	public void join(@NotNull Configurable configurable) {
		cacheFileManager(new Configurable.Editor(host, configurable));
	}

	/**
	 * Retrieve a Config instance via its name and description.
	 * <p>
	 * This method resolves config objects for any {@link com.github.sanctum.panther.file.Configurable.Host}
	 * main class passed through the initial search query and handles automatic caching.
	 *
	 * @param name name of config file
	 * @return existing instance or create new Config
	 * @throws IllegalArgumentException if name is empty
	 */
	public @NotNull Configurable.Editor get(@NotNull final String name) throws IllegalArgumentException {
		return get(name, (String) null);
	}

	/**
	 * Retrieve a Config instance via its name and description.
	 * <p>
	 * This method resolves config objects for any {@link com.github.sanctum.panther.file.Configurable.Host}
	 * main class passed through the initial search query.
	 *
	 * @param name name of config file
	 * @param desc description of config file (designate subdirectory)
	 * @return existing instance or create new Config
	 * @throws IllegalArgumentException if name is empty
	 */
	public @NotNull Configurable.Editor get(@NotNull final String name, @Nullable final String desc) throws IllegalArgumentException {
		return get(name, desc, Configurable.Type.YAML);
	}

	/**
	 * Retrieve a Config instance via its name and description.
	 * <p>
	 * This method resolves config objects for any {@link com.github.sanctum.panther.file.Configurable.Host}
	 * main class passed through the initial search query.
	 *
	 * @param name name of config file
	 * @param data Whether to use JSON or YML, true = JSON
	 * @return existing instance or create new Config
	 * @throws IllegalArgumentException if name is empty
	 */
	public @NotNull Configurable.Editor get(@NotNull final String name, final Configurable.Extension data) throws IllegalArgumentException {
		return get(name, null, data);
	}

	<T> T test(T o) {
		if (o != null) {
			System.out.println("Grabbing cached editor");
		}
		return o;
	}

	/**
	 * Retrieve a Config instance via its name and description.
	 * <p>
	 * This method resolves config objects for any {@link com.github.sanctum.panther.file.Configurable.Host}
	 * main class passed through the initial search query.
	 *
	 * @param name name of config file
	 * @param desc description of config file (designate subdirectory
	 * @param type The file type extension.
	 * @return existing instance or create new Config
	 * @throws IllegalArgumentException if name is empty
	 */
	public @NotNull Configurable.Editor get(@NotNull final String name, @Nullable final String desc, final Configurable.Extension type) throws IllegalArgumentException {
		// move up to fail fast
		if (name.isEmpty()) {
			throw new IllegalArgumentException("Name cannot be empty!");
		}
		// See CACHE declaration above for new key strategy
		return Optional.ofNullable(CACHE.get(host.getName()))
				.map(m -> test(m.get(fixNullDescription(desc) + ';' + name)))
				.filter(m -> type.getClass().isAssignableFrom(m.getRoot().getType().getClass()))
				.orElseGet(() -> cacheFileManager(new Configurable.Editor(host, name, desc, type)));
	}

	/**
	 * This method checks if the desired backing file exists without creating necessary parent locations.
	 * <p>
	 * Do not use this method if the desired file type is not a yml file.
	 *
	 * @param name The name of the file.
	 * @param desc The directory the file belongs to.
	 * @return true if the target file exists. false if either the parent or target file location doesn't exist.
	 */

	public boolean exists(@NotNull final String name, @Nullable final String desc) {
		return exists(name, desc, Configurable.Type.YAML);
	}

	/**
	 * This method checks if the desired backing file exists without creating necessary parent locations.
	 *
	 * @param name      The name of the file.
	 * @param desc      The directory the file belongs to.
	 * @param extension The file extension to use, Ex: "data" or "yml"
	 * @return true if the target file exists. false if either the parent or target file location doesn't exist.
	 */
	public boolean exists(@NotNull final String name, @Nullable final String desc, @NotNull Configurable.Extension extension) {
		// move up to fail fast
		if (name.isEmpty()) {
			throw new IllegalArgumentException("Name cannot be empty!");
		}
		final File parent = (desc == null || desc.isEmpty()) ? host.getDataFolder() : new File(host.getDataFolder(), desc);
		if (!parent.exists()) {
			return false;
		}
		File test = new File(parent, name.concat(extension.get()));
		return test.exists();
	}

	static String fixNullDescription(String d) {
		if (d == null) return "?";
		return d;
	}

	static Configurable.Editor cacheFileManager(Configurable.Editor fileManager) {
		if (CACHE.containsKey(fileManager.host.getName())) {
			Map<String, Configurable.Editor> map = CACHE.get(fileManager.host.getName());
			map.putIfAbsent(fixNullDescription(fileManager.configuration.getDirectory()) + ";" + fileManager.configuration.getName(), fileManager);
		} else {
			CACHE.put(fileManager.host.getName(), new ConcurrentHashMap<>()).put(fileManager.configuration.getDirectory() + ';' + fileManager.configuration.getName(), fileManager);
		}
		return fileManager;
	}

}
