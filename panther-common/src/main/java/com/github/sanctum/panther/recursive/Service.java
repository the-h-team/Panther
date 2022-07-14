package com.github.sanctum.panther.recursive;

import com.github.sanctum.panther.annotation.See;
import com.github.sanctum.panther.util.MapDecompression;
import com.github.sanctum.panther.util.PantherLogger;
import org.jetbrains.annotations.NotNull;

/**
 * An interface used for marking recursively used classes. Can be cached using {@link ServiceFactory}
 */
@See({PantherLogger.class, MapDecompression.class})
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
