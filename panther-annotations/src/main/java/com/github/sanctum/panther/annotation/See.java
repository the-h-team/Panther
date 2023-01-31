package com.github.sanctum.panther.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// FIXME Drop RetentionPolicy.RUNTIME if no reflection use
/**
 * Hints at one or more classes for use in a workflow.
 *
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
public @interface See {

	/**
	 * Gets the class or classes to reference.
	 *
	 * @return the class(es) to reference
	 */
	@NotNull Class<?>[] value();


}
