package com.github.sanctum.panther.recursive;

import com.github.sanctum.panther.container.PantherEntryMap;
import com.github.sanctum.panther.container.PantherMap;
import com.github.sanctum.panther.util.PantherLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A container holding usable services.
 */
public final class ServiceFactory implements ServiceManager {

	static ServiceManager instance;
	final PantherMap<Class<?>, ServiceLoader> services = new PantherEntryMap<>();

	ServiceFactory() {}

	/**
	 * Get a locally cached service manager.
	 *
	 * @return a cached service manager instance.
	 */
	public static @NotNull ServiceManager getInstance() {
		return instance != null ? instance : (instance = new ServiceFactory());
	}

	/**
	 * Overwrite the system service manager for global use.
	 *
	 * @param in The instance to overwrite with.
	 */
	public static void setInstance(@NotNull ServiceManager in) {
		instance = in;
	}

	/**
	 * Get a new service manager instance.
	 *
	 * @return a fresh service manager instance.
	 */
	public static @NotNull ServiceManager newInstance() {
		return new ServiceFactory();
	}

	@Override
	public @Nullable ServiceLoader getLoader(@NotNull Class<?> clazz) {
		return services.get(clazz);
	}

	@Override
	public @NotNull ServiceLoader newLoader(@NotNull Class<?> clazz) {
		if (services.containsKey(clazz)) return services.get(clazz);
		if (!clazz.isAssignableFrom(Service.class)) {
			if (!clazz.isAnnotationPresent(Service.Tag.class)) throw new NullPointerException("Not a known service, services require inheritance from the interface Service or needs a Service.Tag class annotation!");
			Service.Tag r = clazz.getAnnotation(Service.Tag.class);
			PantherLogger.getInstance().getLogger().finest("Registered new annotative service loader: {" + clazz.getName() + "}: " + "[" + '"' + r.value() + '"' + "]");
		}
		ServiceLoader loader = new ServiceLoader(clazz);
		services.put(clazz, loader);
		return loader;
	}

	@Override
	public <T> @Nullable T getService(@NotNull Class<T> clazz) {
		ServiceLoader loader = getLoader(clazz);
		if (loader != null) return loader.load();
		newLoader(clazz);
		return null;
	}

}
