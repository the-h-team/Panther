package com.github.sanctum.panther.recursive;

import com.github.sanctum.panther.container.PantherEntryMap;
import com.github.sanctum.panther.container.PantherMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A container holding usable services.
 */
public final class ServiceFactory {

	static ServiceFactory instance;
	final PantherMap<Class<? extends Service>, ServiceLoader<? extends Service>> services = new PantherEntryMap<>();

	ServiceFactory() {
	}

	/**
	 * @return
	 */
	public static @NotNull ServiceFactory getInstance() {
		return instance != null ? instance : (instance = new ServiceFactory());
	}

	/**
	 * @return
	 */
	public static @NotNull ServiceFactory newInstance() {
		return new ServiceFactory();
	}

	/**
	 * @param clazz
	 * @param <T>
	 * @return
	 */
	public <T extends Service> @Nullable ServiceLoader<T> getLoader(@NotNull Class<T> clazz) {
		return (ServiceLoader<T>) services.get(clazz);
	}

	/**
	 * @param clazz
	 * @param <T>
	 * @return
	 */
	public <T extends Service> @NotNull ServiceLoader<T> newLoader(@NotNull Class<T> clazz) {
		ServiceLoader<T> loader = new ServiceLoader<>(clazz);
		services.put(clazz, loader);
		return loader;
	}

	/**
	 * @param clazz
	 * @param <T>
	 * @return
	 */
	public <T extends Service> @Nullable T getService(@NotNull Class<T> clazz) {
		ServiceLoader<T> loader = getLoader(clazz);
		if (loader != null) return loader.load();
		newLoader(clazz);
		return null;
	}

}
