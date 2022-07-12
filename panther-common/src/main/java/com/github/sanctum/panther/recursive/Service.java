package com.github.sanctum.panther.recursive;

import org.jetbrains.annotations.NotNull;

/**
 * An interface used for marking recursively used classes. Can be cached using {@link ServiceFactory}
 */
public interface Service {

	/**
	 * @return
	 */
	default @NotNull Obligation getObligation() {
		return () -> "Not specified.";
	}

	/**
	 *
	 */
	@FunctionalInterface
	interface Obligation {

		/**
		 * @return
		 */
		@NotNull String get();

	}

}
