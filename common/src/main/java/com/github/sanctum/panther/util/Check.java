package com.github.sanctum.panther.util;

import com.github.sanctum.panther.annotation.AnnotationDiscovery;
import com.github.sanctum.panther.annotation.Experimental;
import com.github.sanctum.panther.annotation.Json;
import com.github.sanctum.panther.annotation.Note;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Check {

	public static void argument(boolean b, @Nullable String message) {
		if (!b) {
			throw new IllegalArgumentException(message);
		}
	}

	public static @Json
	boolean isJson(@NotNull String string) {
		return string.startsWith("{") || string.startsWith("[") && string.endsWith("{") || string.endsWith("[");
	}

	public static @Json String forJson(@NotNull String string, @NotNull String message) {
		if (!isJson(string)) {
			throw new IllegalArgumentException(message);
		}
		return string;
	}

	public static boolean isNull(Object... o) {
		for (Object ob : o) {
			if (ob != null) return false;
		}
		return true;
	}

	public static <T> @NotNull T forNull(T t) {
		if (t == null) throw new NullPointerException("Value cannot be null!");
		return forWarnings(t);
	}

	public static <T> @NotNull T forNull(T t, String message) {
		if (t == null) throw new NullPointerException(message);
		return forWarnings(t);
	}

	public static <T> @NotNull T forWarnings(T t) {
		if (t == null) throw new IllegalArgumentException("Value cannot be null!");
		AnnotationDiscovery<Experimental, Object> experimentalAnnotationDiscovery = AnnotationDiscovery.of(Experimental.class, t);
		experimentalAnnotationDiscovery.filter(m -> Arrays.stream(m.getParameters()).anyMatch(p -> p.isAnnotationPresent(Experimental.class)) || m.isAnnotationPresent(Experimental.class));
		AnnotationDiscovery<Note, Object> noteAnnotationDiscovery = AnnotationDiscovery.of(Note.class, t);
		noteAnnotationDiscovery.filter(m -> Arrays.stream(m.getParameters()).anyMatch(p -> p.isAnnotationPresent(Note.class)) || m.isAnnotationPresent(Note.class));
		Logger message = PantherLogger.getInstance().getLogger();
		if (experimentalAnnotationDiscovery.isPresent()) {
			message.warning("- Warning scan found (" + experimentalAnnotationDiscovery.count() + ") methods at checkout for object '" + t.getClass().getSimpleName() + "'");
			if (t.getClass().isAnnotationPresent(Experimental.class)) {
				Experimental e = t.getClass().getAnnotation(Experimental.class);
				message.warning("- Entire class " + t.getClass().getSimpleName() + " found with warning '" + e.dueTo() + "'");
			}
			experimentalAnnotationDiscovery.ifPresent((r, m) -> {
				message.warning("- Method " + m.getName() + " found with warning '" + r.dueTo() + "'");
			});
			noteAnnotationDiscovery.ifPresent((r, m) -> {
				message.info("- Method " + m.getName() + " found with note '" + r.value() + "'");
			});
		} else {
			if (t.getClass().isAnnotationPresent(Experimental.class)) {
				Experimental e = t.getClass().getAnnotation(Experimental.class);
				message.warning("- Class " + t.getClass().getSimpleName() + " found with warning '" + e.dueTo() + "'");
			}
			if (t.getClass().isAnnotationPresent(Note.class)) {
				Note e = t.getClass().getAnnotation(Note.class);
				message.info("- Class " + t.getClass().getSimpleName() + " found with note '" + e.value() + "'");
			}
		}
		return t;
	}

	public static <A extends Annotation, T> @NotNull T forAnnotation(T t, Class<A> annotative, AnnotationDiscovery.AnnotativeConsumer<A, Method, String> function) {
		return forAnnotation(t, annotative, function, false);
	}

	public static <A extends Annotation, T> @NotNull T forAnnotation(T t, Class<A> annotative, AnnotationDiscovery.AnnotativeConsumer<A, Method, String> function, boolean warning) {
		if (t == null) throw new IllegalArgumentException("Value cannot be null!");
		AnnotationDiscovery<A, Object> discovery = AnnotationDiscovery.of(annotative, t);
		discovery.filter(m -> Arrays.stream(m.getParameters()).anyMatch(p -> p.isAnnotationPresent(annotative)) || m.isAnnotationPresent(annotative));
		Logger message = PantherLogger.getInstance().getLogger();
		if (discovery.isPresent()) {
			if (warning) {
				message.info("- Warning scan found (" + discovery.count() + ") methods at checkout.");
				discovery.ifPresent((r, m) -> message.warning(function.accept(r, m)));
			} else {
				message.info("- Info scan found (" + discovery.count() + ") methods at checkout.");
				discovery.ifPresent((r, m) -> message.info(function.accept(r, m)));
			}
		} else {
			if (t.getClass().isAnnotationPresent(annotative)) {
				A e = t.getClass().getAnnotation(annotative);
				if (warning) {
					message.warning(function.accept(e, t.getClass().getMethods()[0]));
				} else {
					message.info(function.accept(e, t.getClass().getMethods()[0]));
				}
			}
		}
		return t;
	}


}
