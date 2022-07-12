package com.github.sanctum.panther.util;

/**
 *
 */
@FunctionalInterface
public interface TripleWideConsumer<T, W, C> {

	void accept(T t, W w, C c);

}
