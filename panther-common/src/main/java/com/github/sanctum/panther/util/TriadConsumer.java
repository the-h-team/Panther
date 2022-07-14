package com.github.sanctum.panther.util;

/**
 * A 3 element {@link java.util.function.BiConsumer} like interface.
 */
@FunctionalInterface
public interface TriadConsumer<T, U, F> {

	void accept(T t, U u, F f);

}
