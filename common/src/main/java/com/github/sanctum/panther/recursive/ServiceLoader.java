package com.github.sanctum.panther.recursive;

import com.github.sanctum.panther.util.EasyTypeAdapter;
import com.github.sanctum.panther.util.TypeAdapter;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

/**
 * A container for instances on service objects.
 */
public final class ServiceLoader {

	final Class<?> classHandle;
	Supplier<?> serviceSupplier;
	Object service;

	public ServiceLoader(@NotNull Class<?> handle) {
		this.classHandle = handle;
	}

	/**
	 * Supply an already instantiated service instance
	 *
	 * @param service the service to supply.
	 * @return The same service loader instance.
	 */
	public <R> ServiceLoader supply(@NotNull R service) {
		if (!classHandle.isAssignableFrom(service.getClass()))
			throw new IllegalStateException("Class " + service.getClass().getSimpleName() + " does not inherit from " + classHandle.getSimpleName());
		this.service = service;
		return this;
	}

	/**
	 * Supply a service builder.
	 *
	 * @param supplier the supplier to use.
	 * @return The same service loader instance.
	 */
	public <R> ServiceLoader supplyFresh(@NotNull Supplier<R> supplier) {
		TypeAdapter<R> adapter = new EasyTypeAdapter<R>(){};
		if (!classHandle.isAssignableFrom(adapter.getType()))
			throw new IllegalStateException("Class " + adapter.getType().getSimpleName() + " does not inherit from " + classHandle.getSimpleName());
		this.serviceSupplier = supplier;
		return this;
	}

	/**
	 * Load the cached service instance / builder instance.
	 *
	 * @return the cached service or null if not provided.
	 */
	public <R> @NotNull("A service must be initialized!") R load() {
		Class<R> c = (Class<R>) classHandle;
		return service != null ? c.cast(service) : c.cast(serviceSupplier.get());
	}
}
