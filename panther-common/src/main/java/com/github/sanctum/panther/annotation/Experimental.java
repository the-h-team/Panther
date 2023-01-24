package com.github.sanctum.panther.annotation;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that an API member (a class, method, parameter or field) is not in
 * stable state yet. It may be renamed, changed or removed in a future version.
 * <p>
 * <strong>This annotation should be used on API elements only--it communicates
 * merely <em>the stability of an interface</em>--not the stability or the
 * performance of its underlying implementation.</strong>
 * It is generally safe to use an element marked with this annotation--though
 * unstable API may require closer study of its related dev comments. If,
 * however, the element belongs to an external library linkage it might also be
 * that the annotation is used to forecast likely linkage issues if/when that
 * library is updated to a new version.</p>
 *
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.CONSTRUCTOR})
public @interface Experimental {

	/**
	 * Gets the developer comment about this element.
	 *
	 * @return the developer comment
	 */
	@NotNull String dueTo() default "";

	/**
	 * @deprecated see {@link Removal}
	 * @return Whether the element this annotation belongs to is set for removal.
	 */
	@Deprecated
	@Removal(inVersion = "1.0.2")
	@ApiStatus.ScheduledForRemoval(inVersion = "1.0.2")
	boolean atRisk() default false;

}
