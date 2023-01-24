package com.github.sanctum.panther.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// TODO find runtime use (debugging perhaps) or deprecate entirely. Normal comments, javadoc are sufficient
/**
 * A developer comment not included in the Javadoc.
 *
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.ANNOTATION_TYPE, ElementType.TYPE_USE})
public @interface Comment {

	/**
	 * Gets the developer comment.
	 * <p>Defaults to "".
	 *
	 * @return the developer comment
	 */
	@NotNull String value() default "";

	/**
	 * Gets the author(s) of the comment.
	 *
	 * @return the author(s) of the comment
	 */
	@NotNull String[] author() default {};

}
