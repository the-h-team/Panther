package com.github.sanctum.panther.recursive;

import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

/**
 * @param <T>
 */
public final class ServiceLoader<T extends Service> {

	final Class<T> classHandle;
	Supplier<T> serviceSupplier;
	T service;

	ServiceLoader(@NotNull Class<T> handle) {
		this.classHandle = handle;
	}

	/**
	 * @param service
	 * @return
	 */
	public ServiceLoader<T> supply(@NotNull T service) {
		this.service = service;
		return this;
	}

	/**
	 * @param supplier
	 * @return
	 */
	public ServiceLoader<T> supplyFresh(@NotNull Supplier<T> supplier) {
		this.serviceSupplier = supplier;
		return this;
	}

	/**
	 * @return
	 */
	public @NotNull("A service must be initialized!") T load() {
		return service != null ? service : serviceSupplier.get();
	}
}
