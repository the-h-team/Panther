package com.github.sanctum.panther.placeholder;

import com.github.sanctum.panther.container.PantherCollection;
import com.github.sanctum.panther.container.PantherEntryMap;
import com.github.sanctum.panther.container.PantherMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An abstraction dedicated to managing placeholder translation requests & additions.
 */
public abstract class PlaceholderRegistration {

	static PlaceholderTranslationUtility instance;
	static final PantherMap<String, Map<String, Placeholder>> history = new PantherEntryMap<>();


	public static @NotNull
	PlaceholderRegistration getInstance() {
		if (instance == null) {
			instance = new PlaceholderTranslationUtility() {
			};
		}
		return instance.registration;
	}

	/**
	 * Register a custom placeholder translation into cache.
	 *
	 * @param translation The translation to register.
	 */
	public abstract void registerTranslation(@NotNull Placeholder.Translation translation);

	/**
	 * Remove a registered translation from cache.
	 *
	 * @param translation The translation to remove.
	 */
	public abstract void unregisterTranslation(@NotNull Placeholder.Translation translation);

	/**
	 * Get a registered translation by its identifier.
	 *
	 * @param identifier The identifier to use.
	 * @return The registered translation or null.
	 */
	public abstract Placeholder.Translation getTranslation(@NotNull Placeholder.Identifier identifier);

	/**
	 * Get a registered translation by its name.
	 *
	 * @param name The identifier to use.
	 * @return The registered translation or null.
	 */
	public abstract Placeholder.Translation getTranslation(@NotNull String name);

	/**
	 * A forEach method on cached translation objects.
	 *
	 * @param consumer The consuming operation to enact.
	 */
	public abstract void runAction(@NotNull Consumer<Placeholder.Translation> consumer);

	/**
	 * Check if a specific translation is registered into cache.
	 *
	 * @param translation The translation to check
	 * @return true if the translation is registered.
	 */
	public abstract boolean isRegistered(@NotNull Placeholder.Translation translation);

	/**
	 * Check if a string contains any registered placeholders.
	 *
	 * @param text The string to check
	 * @return false if the string contains placeholders.
	 */
	public abstract boolean isEmpty(@NotNull String text);

	/**
	 * Check if a string contains any registered placeholders under the specified identity.
	 *
	 * @param text       The string to check
	 * @param identifier The identity
	 * @return false if the string contains placeholders regarding the specified identity.
	 */
	public abstract boolean isEmpty(@NotNull String text, @Nullable Placeholder.Identifier identifier);

	/**
	 * Replace all possible placeholders in the provided string.
	 *
	 * @param text The string to format
	 * @return A placeholder formatted string.
	 */
	public abstract @NotNull String replaceAll(@NotNull String text);

	/**
	 * Replace all possible placeholders in the provided string using a custom variable.
	 *
	 * @param text     The string to format
	 * @param receiver The variable to provide for context
	 * @return A placeholder formatted string.
	 */
	public abstract @NotNull String replaceAll(@NotNull String text, @Nullable Object receiver);

	/**
	 * Replace all possible placeholders in the provided string using a custom variable
	 *
	 * @param text     The string to format
	 * @param receiver The variable to provide for context
	 * @return A placeholder formatted string
	 */
	public abstract @NotNull String replaceAll(@NotNull String text, @Nullable Placeholder.Variable receiver);

	/**
	 * Replace all possible placeholders in the provided string using a custom variable and placeholder inquiry.
	 *
	 * @param text        The string to format
	 * @param receiver    The variable to provide for context
	 * @param placeholder The placeholder to format
	 * @return A placeholder formatted string.
	 */
	public abstract @NotNull String replaceAll(@NotNull String text, @Nullable Placeholder.Variable receiver, Placeholder placeholder);

	/**
	 * Replace all possible placeholders in the provided string using a custom variable, placeholder inquiry & identity.
	 *
	 * @param text        The string to format
	 * @param receiver    The variable to provide for context
	 * @param identifier  The identity
	 * @param placeholder The placeholder to format
	 * @return A placeholder formatted string.
	 */
	public abstract @NotNull String replaceAll(@NotNull String text, @Nullable Placeholder.Variable receiver, @Nullable Placeholder.Identifier identifier, @NotNull Placeholder placeholder);

	/**
	 * @param text
	 * @param placeholder
	 * @param replacement
	 * @return
	 */
	public abstract @NotNull String replaceAll(@NotNull String text, @NotNull Placeholder placeholder, @NotNull String replacement);

	/**
	 * @param text
	 * @param placeholder
	 * @return
	 */
	public abstract @Nullable String findFirst(@NotNull String text, @NotNull Placeholder placeholder);

	/**
	 * @param text
	 * @param placeholder
	 * @return
	 */
	public abstract @NotNull PantherCollection<String> findAny(@NotNull String text, @NotNull Placeholder placeholder);

	/**
	 * @param text
	 * @param placeholder
	 * @return
	 */
	public abstract @NotNull Placeholder[] findAny(@NotNull String text, @NotNull Placeholder.Translation placeholder);

	/**
	 * @return
	 */
	public final PantherMap<Placeholder.Identifier, List<Placeholder>> getHistory() {
		PantherMap<Placeholder.Identifier, List<Placeholder>> map = new PantherEntryMap<>();
		history.forEach(entry -> {
			Placeholder.Identifier identifier = entry::getKey;
			List<Placeholder> placeholders = new ArrayList<>(entry.getValue().values());
			map.put(identifier, placeholders);
		});
		return map;
	}


}
