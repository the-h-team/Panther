package com.github.sanctum.panther.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

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
 * library is updated to a new version.
 * <p>
 * This annotation very closely resembles
 * {@link org.jetbrains.annotations.ApiStatus.Experimental} with the additional
 * feature that it is retained at runtime. This facilitates program logic based
 * on the presence and/or content of this annotation.
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
	 * Checks whether this element is at risk of removal.
	 * <p>
	 * What exactly is meant by "at risk of removal" is left to the developer.
	 * You may refer to the developer comment (if present) for details.
	 *
	 * @return true when the element is at risk of removal
	 */
	boolean atRisk() default false;

}
