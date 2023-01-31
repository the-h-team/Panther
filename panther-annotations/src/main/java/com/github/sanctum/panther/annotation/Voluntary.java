package com.github.sanctum.panther.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// FIXME just use overloads if the only target is a varargs param
//  In most other cases we could use @Nullable
/**
 * Marks elements that are completely optional.
 *
 * @since 1.0.0
 */
@Documented
// FIXME Drop RetentionPolicy.RUNTIME if no reflection use
@Retention(RetentionPolicy.RUNTIME)
// FIXME narrow target to PARAMETER?
@Target(value = {ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.ANNOTATION_TYPE, ElementType.TYPE_USE})
public @interface Voluntary {

	/**
	 * Gets the developer-supplied reason for this element being optional.
	 *
	 * @return the reason this element is optional
	 */
	@NotNull String value() default "";

}
