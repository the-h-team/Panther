package com.github.sanctum.panther.annotation;

import com.github.sanctum.panther.util.DummyReducer;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// TODO consider replacing functionality with @Language("JSON")-based meta annotation; ask me about it -ms5984
/**
 * Marks methods, fields and parameters which work with String JSON.
 *
 * @since 1.0.0
 */
@Documented
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.TYPE_PARAMETER, ElementType.METHOD})
public @interface Json {

	// FIXME pull off key into a separate annotation; otherwise remove default + remove all no-arg usages
	@NotNull String key() default "";

	// FIXME pull off reducer into a separate annotation
	Class<? extends Reducer> reducer() default DummyReducer.class;

	// FIXME document Reducer system
	interface Reducer {

		Object reduce(Object t);

	}

}
