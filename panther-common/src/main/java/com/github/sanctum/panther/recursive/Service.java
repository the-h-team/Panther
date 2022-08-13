package com.github.sanctum.panther.recursive;

import com.github.sanctum.panther.annotation.See;
import com.github.sanctum.panther.util.MapDecompression;
import com.github.sanctum.panther.util.PantherLogger;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jetbrains.annotations.NotNull;

/**
 * An interface used for marking recursively used classes. Can be cached using {@link ServiceFactory}
 */
@See({PantherLogger.class, MapDecompression.class})
public interface Service {

	/**
	 * @return the obligation for this service.
	 */
	default @NotNull Obligation getObligation() {
		return () -> "Not specified.";
	}

	/**
	 * A string holder for services retaining duty knowledge on a given service.
	 */
	@FunctionalInterface
	interface Obligation {

		/**
		 * @return the obligation.
		 */
		@NotNull String get();

	}

	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(value = {ElementType.TYPE})
	@interface Tag {

		@NotNull String value();

	}
}
