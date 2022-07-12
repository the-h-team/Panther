package com.github.sanctum.panther.placeholder;

import java.util.Arrays;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This interface is set to be a flag for numerous types of placeholder prefix & suffix combinations and can also contain {@link Placeholder#parameters()}
 */
public interface Placeholder {

	/**
	 * "<>"
	 */
	Placeholder ANGLE_BRACKETS = new Placeholder() {
		@Override
		public char start() {
			return '<';
		}

		@Override
		public boolean isDefault() {
			return true;
		}

		@Override
		public char end() {
			return '>';
		}
	};
	/**
	 * "{}"
	 */
	Placeholder CURLEY_BRACKETS = new Placeholder() {
		@Override
		public char start() {
			return '{';
		}

		@Override
		public boolean isDefault() {
			return true;
		}

		@Override
		public char end() {
			return '}';
		}
	};
	/**
	 * "%%"
	 */
	Placeholder PERCENT = new Placeholder() {
		@Override
		public char start() {
			return '%';
		}

		@Override
		public boolean isDefault() {
			return true;
		}

		@Override
		public char end() {
			return '%';
		}
	};


	/**
	 * @return
	 */
	char start();

	/**
	 * @return
	 */
	default CharSequence parameters() {
		return "";
	}

	/**
	 * @return
	 */
	char end();

	/**
	 * Check if this placeholder is equal to another.
	 *
	 * @param placeholder The placeholder to compare.
	 * @return true if the placeholders are the same.
	 */
	default boolean isSame(Placeholder placeholder) {
		return start() == placeholder.start() && end() == placeholder.end() && parameters().equals(placeholder.parameters());
	}

	/**
	 * Check if this placeholder is equal to a set of characters.
	 *
	 * @param start The starting character.
	 * @param end   The ending character.
	 * @return true if this placeholder has the same prefix and suffix regex.
	 */
	default boolean isSame(char start, char end) {
		return start() == start && end() == end;
	}

	/**
	 * Check if this placeholder contains no parameters.
	 *
	 * @return true if this placeholder is only a prefix & suffix.
	 */
	default boolean isEmpty() {
		return parameters().length() == 0 || parameters().equals(" ");
	}

	/**
	 * Check if this placeholder is default.
	 *
	 * @return true if this placeholder is default.
	 */
	default boolean isDefault() {
		return false;
	}

	/**
	 * Get this placeholder and parameters as a string.
	 *
	 * @return this placeholder as a string.
	 */
	default @NotNull String toRaw() {
		return String.valueOf(start()) + parameters() + end();
	}

	/**
	 * Get this placeholder and parameters as a translated string.
	 *
	 * @param variable The optional translation variable.
	 * @return this placeholder translated.
	 */
	default @NotNull String toTranslated(@Nullable Placeholder.Variable variable) {
		return PlaceholderRegistration.getInstance().replaceAll(toRaw(), variable, this);
	}

	/**
	 * @return
	 */
	static Placeholder[] values() {
		return new Placeholder[]{ANGLE_BRACKETS, CURLEY_BRACKETS, PERCENT};
	}

	/**
	 * @param start
	 * @param end
	 * @return
	 */
	static @NotNull Placeholder valueOf(char start, char end) {
		return Arrays.stream(values()).filter(v -> v.isSame(start, end)).findFirst().orElse(new Placeholder() {
			@Override
			public char start() {
				return start;
			}

			@Override
			public char end() {
				return end;
			}
		});
	}


	/**
	 * An interface  describing a primary identifier for a placeholder translation.
	 */
	@FunctionalInterface
	interface Identifier {

		@NotNull String get();

		default @NotNull String spacer() {
			return "_";
		}

	}

	/**
	 * An interface for converting placeholder parameters into any string of choice.
	 */
	@FunctionalInterface
	interface Translation {

		@Nullable String onTranslation(String parameter, Variable variable);

		/**
		 * Get the creator information for this implementation.
		 *
		 * @return The information for this translation or null.
		 */
		@Nullable
		default Placeholder.Signature getInformation() {
			return null;
		}

		/**
		 * Get all the placeholders this implementation uses.
		 *
		 * @return an array of placeholders used with this translation.
		 */
		@NotNull
		default Placeholder[] getPlaceholders() {
			return values();
		}

		/**
		 * Get this translation's special identifier if it has one.
		 *
		 * @return the special identifier for this translation or null.
		 */
		@Nullable
		default Placeholder.Identifier getIdentifier() {
			return null;
		}

		/**
		 * Get all found (registered) placeholders within the given text.
		 *
		 * @param text the text to search.
		 * @return an array of placeholders or empty if none.
		 */
		default Placeholder[] getPlaceholders(String text) {
			return PlaceholderRegistration.getInstance().findAny(text, this);
		}

		/**
		 * Check if this translation uses a special identifier.
		 *
		 * @return false if this identifier is null.
		 */
		default boolean hasCustomIdentifier() {
			return getIdentifier() != null;
		}

		/**
		 * Check if this translation is registered.
		 *
		 * @return true if this translation is registered into cache.
		 */
		default boolean isRegistered() {
			return PlaceholderRegistration.getInstance().isRegistered(this);
		}

		/**
		 * Register this translation to cache.
		 */
		default void register() {
			PlaceholderRegistration.getInstance().registerTranslation(this);
		}

		/**
		 * Remove this translation from cache.
		 */
		default void unregister() {
			PlaceholderRegistration.getInstance().unregisterTranslation(this);
		}


	}

	/**
	 *
	 */
	@FunctionalInterface
	interface Variable {

		/**
		 * @return
		 */
		Object get();

		/**
		 * @return
		 */
		default boolean exists() {
			return get() != null;
		}

	}

	/**
	 * An interface for development information related to {@link Translation} like the creator's of the
	 * implementation and the version and name of the implementation.
	 */
	interface Signature {

		/**
		 * @return the name for this implementation.
		 */
		@NotNull String getName();

		@NotNull String[] getAuthors();

		@NotNull String getVersion();


	}
}
