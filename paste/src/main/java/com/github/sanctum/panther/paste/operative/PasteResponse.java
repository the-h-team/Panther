package com.github.sanctum.panther.paste.operative;

import com.github.sanctum.panther.paste.option.Context;
import com.github.sanctum.panther.util.PantherString;
import java.util.Arrays;

/**
 * A child component of {@link Context} for retaining information retrieved from a web connection.
 */
@FunctionalInterface
public interface PasteResponse extends Context {

	/**
	 * @return all responses from this web interaction.
	 */
	default String[] getAll() {
		return get().split("");
	}

	/**
	 * @return true if this response is valid.
	 */
	default boolean isValid() {
		return get() != null && !get().equals("NA");
	}

	/**
	 * @return true if this response is just a domain address.
	 */
	default boolean isLink() {
		return get().startsWith("http");
	}

	/**
	 * @return true if this response contains a link.
	 */
	default boolean containsLink() {
		return new PantherString(get()).contains("http", "https") || Arrays.stream(getAll()).anyMatch(s -> new PantherString(s).contains("http", "https"));
	}

}
