package com.github.sanctum.panther.util;

import java.util.function.BiFunction;

/**
 * A Bi-Function like interface for converting 2 objects into one result.
 *
 * @see java.util.function.Function
 */
public interface WideFunction<W, F, R> extends BiFunction<W, F, R> {}
