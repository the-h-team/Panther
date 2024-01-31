package com.github.sanctum.panther.recursive;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ServiceManager {

	/**
	 * Get a cached service loader from this manager.
	 *
	 * @param clazz The service.
	 * @return a cached service loader or null if one is not provided.
	 */
	@Nullable ServiceLoader getLoader(@NotNull Class<?> clazz);

	/**
	 * Get a new service loader instance from this manager.
	 * If an instance is already stored it will be returned instead.
	 *
	 * @param clazz The service.
	 * @return a new service loader or an already cached one if one is found.
	 */
	@NotNull ServiceLoader newLoader(@NotNull Class<?> clazz);

	/**
	 * Get a loaded service from this manager.
	 *
	 * @param clazz The service.
	 * @param <T> The service type.
	 * @return a cached service or null if one is not provided.
	 */
	<T> @Nullable T getService(@NotNull Class<T> clazz);

}
