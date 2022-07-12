package com.github.sanctum.panther.util;

import java.util.function.BiConsumer;

/**
 * A Bi-Consumer like interface for operating on two elements at once.
 *
 * @see java.util.function.BiConsumer
 */
public interface WideConsumer<W, C> extends BiConsumer<W, C> {}
