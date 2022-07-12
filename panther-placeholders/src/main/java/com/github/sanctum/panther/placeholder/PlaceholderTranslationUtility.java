package com.github.sanctum.panther.placeholder;

import com.github.sanctum.panther.container.ImmutablePantherCollection;
import com.github.sanctum.panther.container.PantherCollection;
import com.github.sanctum.panther.container.PantherList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class PlaceholderTranslationUtility {

	final PlaceholderRegistration registration;

	PlaceholderTranslationUtility() {
		this.registration = new PlaceholderRegistration() {

			private final PantherCollection<Placeholder.Translation> translations = new PantherList<>();

			@Override
			public void registerTranslation(@NotNull Placeholder.Translation translation) {
				translations.add(translation);
			}

			@Override
			public void unregisterTranslation(@NotNull Placeholder.Translation translation) {
				translations.remove(translation);
			}

			@Override
			public Placeholder.Translation getTranslation(@NotNull Placeholder.Identifier identifier) {
				return translations.stream().filter(placeholderTranslation -> placeholderTranslation.hasCustomIdentifier() && placeholderTranslation.getIdentifier().get().equals(identifier.get())).findFirst().orElse(null);
			}

			@Override
			public Placeholder.Translation getTranslation(@NotNull String name) {
				return translations.stream().filter(translation -> translation.getInformation() != null && translation.getInformation().getName().equals(name)).findFirst().orElse(null);
			}

			@Override
			public void runAction(@NotNull Consumer<Placeholder.Translation> consumer) {
				translations.forEach(consumer);
			}

			@Override
			public boolean isRegistered(@NotNull Placeholder.Translation translation) {
				return translations.contains(translation);
			}

			@Override
			public boolean isEmpty(@NotNull String text) {
				return isEmpty(text, null);
			}

			@Override
			public boolean isEmpty(@NotNull String text, @Nullable Placeholder.Identifier identifier) {
				boolean empty = false;
				Iterator<Placeholder.Translation> conversionIterator = translations.iterator();
				do {
					Placeholder.Translation conversion = conversionIterator.next();
					for (Placeholder hold : conversion.getPlaceholders()) {
						empty = PlaceholderTranslationUtility.this.isEmpty(text, identifier != null ? identifier : conversion.getIdentifier(), hold);
					}
				} while (conversionIterator.hasNext());
				return empty;
			}

			@Override
			public @NotNull String replaceAll(@NotNull String text) {
				if (translations.isEmpty()) return text;
				return replaceAll(text, null);
			}

			@Override
			public @NotNull String replaceAll(@NotNull String text, @Nullable Object receiver) {
				if (translations.isEmpty()) return text;
				String result = text;
				final Placeholder.Variable variable = () -> receiver;
				Iterator<Placeholder.Translation> conversionIterator = translations.iterator();
				do {
					Placeholder.Translation conversion = conversionIterator.next();
					for (Placeholder hold : conversion.getPlaceholders()) {
						result = replaceAll(result, variable, conversion.getIdentifier(), hold);
					}
				} while (conversionIterator.hasNext());
				return result;
			}

			@Override
			public @NotNull String replaceAll(@NotNull String text, @Nullable Placeholder.Variable receiver) {
				if (translations.isEmpty()) return text;
				String result = text;
				Iterator<Placeholder.Translation> conversionIterator = translations.iterator();
				do {
					Placeholder.Translation conversion = conversionIterator.next();
					for (Placeholder hold : conversion.getPlaceholders()) {
						result = replaceAll(result, receiver, conversion.getIdentifier(), hold);
					}
				} while (conversionIterator.hasNext());
				return result;
			}

			@Override
			public @NotNull String replaceAll(@NotNull String text, @Nullable Placeholder.Variable receiver, Placeholder placeholder) {
				if (translations.isEmpty()) return text;
				return replaceAll(text, receiver, null, placeholder);
			}

			@Override
			public @NotNull String replaceAll(@NotNull String text, @Nullable Placeholder.Variable receiver, @Nullable Placeholder.Identifier identifier, @NotNull Placeholder placeholder) {
				if (translations.isEmpty()) return text;
				return PlaceholderTranslationUtility.this.getTranslation(text, identifier, receiver, placeholder);
			}

			@Override
			public @NotNull String replaceAll(@NotNull String text, @NotNull Placeholder placeholder, @NotNull String replacement) {
				Pattern pattern = getPattern(null, placeholder);
				Matcher matcher = pattern.matcher(text);
				if (!matcher.find())
					return text;
				StringBuffer builder = new StringBuffer();
				do {
					matcher.appendReplacement(builder, replacement);
				} while (matcher.find());
				return matcher.appendTail(builder).toString();
			}

			@Override
			public @Nullable String findFirst(@NotNull String text, @NotNull Placeholder placeholder) {
				Pattern pattern = getPattern(null, placeholder);
				Matcher matcher = pattern.matcher(text);
				if (!matcher.find())
					return null;
				return matcher.group("parameters");
			}

			@Override
			public @NotNull PantherCollection<String> findAny(@NotNull String text, @NotNull Placeholder placeholder) {
				ImmutablePantherCollection.Builder<String> builder = ImmutablePantherCollection.builder();
				Pattern pattern = getPattern(null, placeholder);
				Matcher matcher = pattern.matcher(text);
				StringBuffer buffer = new StringBuffer();
				if (!matcher.find())
					return new PantherList<>();
				do {
					builder.add(matcher.group("parameters"));
					matcher.appendReplacement(buffer, "");
				} while (matcher.find());
				return builder.build();
			}

			@Override
			public @NotNull Placeholder[] findAny(@NotNull String text, @NotNull Placeholder.Translation placeholder) {
				return getPlaceholders(text, placeholder);
			}
		};
	}

	boolean isEmpty(String text, Placeholder.Identifier identifier, Placeholder placeholder) {
		Pattern pattern = getPattern(identifier, placeholder);
		Matcher matcher = pattern.matcher(text);
		return !matcher.find();
	}

	@NotNull String getTranslation(String text, Placeholder.Identifier identifier, Placeholder.Variable receiver, Placeholder placeholder) {
		Pattern pattern = getPattern(identifier, placeholder);
		Matcher matcher = pattern.matcher(text);
		if (!matcher.find())
			return text;
		StringBuffer builder = new StringBuffer();
		do {
			if (identifier != null) {
				String actualId = matcher.group("identifier");
				String parameters = matcher.group("parameters");
				Placeholder.Translation conversion = PlaceholderRegistration.getInstance().getTranslation(identifier);
				if (conversion != null) {
					String translation = conversion.onTranslation(parameters, receiver != null ? receiver : () -> null);
					Map<String, Placeholder> placeholderMap = PlaceholderRegistration.history.get(identifier.get() + identifier.spacer());
					final boolean valid = translation != null && !translation.equals(parameters) && !translation.isEmpty();
					if (placeholderMap != null) {
						if (placeholderMap.get(placeholder.start() + parameters.toLowerCase(Locale.ROOT) + placeholder.end()) == null) {
							if (valid) {
								Placeholder record = new Placeholder() {
									@Override
									public char start() {
										return placeholder.start();
									}

									@Override
									public CharSequence parameters() {
										return parameters;
									}

									@Override
									public char end() {
										return placeholder.end();
									}
								};
								placeholderMap.put(placeholder.start() + parameters.toLowerCase(Locale.ROOT) + placeholder.end(), record);
							}
						}
					} else {
						Map<String, Placeholder> map = new HashMap<>();
						if (valid) {
							Placeholder record = new Placeholder() {
								@Override
								public char start() {
									return placeholder.start();
								}

								@Override
								public CharSequence parameters() {
									return parameters;
								}

								@Override
								public char end() {
									return placeholder.end();
								}
							};
							map.put(placeholder.start() + parameters.toLowerCase(Locale.ROOT) + placeholder.end(), record);
						}
						PlaceholderRegistration.history.put(identifier.get() + identifier.spacer(), map);
					}
					matcher.appendReplacement(builder, translation != null ? translation : (placeholder.start() + actualId + identifier.spacer() + parameters + placeholder.end()));
				} else {
					matcher.appendReplacement(builder, placeholder.start() + actualId + identifier.spacer() + parameters + placeholder.end());
				}
			} else {
				PlaceholderRegistration.getInstance().runAction(conversion -> {
					String parameters = matcher.group("parameters");
					if (parameters != null && !parameters.isEmpty()) {
						String translation = conversion.onTranslation(parameters, receiver != null ? receiver : () -> null);
						matcher.appendReplacement(builder, translation != null && !translation.isEmpty() ? translation : (placeholder.start() + parameters + placeholder.end()));
					}
				});
			}
		} while (matcher.find());
		return matcher.appendTail(builder).toString();
	}

	@NotNull Placeholder[] getPlaceholders(String text, Placeholder.Translation conversion) {
		List<Placeholder> placeholderList = new ArrayList<>();
		for (Placeholder p : conversion.getPlaceholders()) {
			Pattern pattern = getPattern(conversion.getIdentifier(), p);
			Matcher matcher = pattern.matcher(text);
			if (!matcher.find()) continue;
			do {
				String parameters = matcher.group("parameters");
				placeholderList.add(new Placeholder() {
					@Override
					public char start() {
						return p.start();
					}

					@Override
					public CharSequence parameters() {
						return parameters;
					}

					@Override
					public char end() {
						return p.end();
					}
				});
			} while (matcher.find());
		}
		return placeholderList.toArray(new Placeholder[0]);
	}

	@NotNull Pattern getPattern(Placeholder.Identifier identifier, Placeholder placeholder) {
		Pattern pattern;
		if (identifier == null) {
			pattern = Pattern.compile(String.format("\\%s(?<parameters>[^%s%s]+)\\%s", placeholder.start(),
					placeholder.start(), placeholder.end(), placeholder.end()));
		} else {
			pattern = Pattern.compile(String.format("\\%s((?<identifier>[a-zA-Z0-9]+)" + identifier.spacer() + ")(?<parameters>[^%s%s]+)\\%s", placeholder.start(),
					placeholder.start(), placeholder.end(), placeholder.end()));
		}
		return pattern;
	}

}
