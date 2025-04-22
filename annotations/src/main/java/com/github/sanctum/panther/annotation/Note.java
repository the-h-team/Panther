package com.github.sanctum.panther.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Adds to elements notes accessible with runtime inspection.
 *
 * @since 1.0.0
 * @author Hempfest
 * @author Matt
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.ANNOTATION_TYPE, ElementType.TYPE_USE})
public @interface Note {

	/**
	 * Gets the notes for this element.
	 *
	 * @return the notes or details for a given element
	 */
	@NotNull String value() default "";

}
