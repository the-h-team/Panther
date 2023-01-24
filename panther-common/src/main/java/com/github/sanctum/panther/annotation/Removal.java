package com.github.sanctum.panther.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// FIXME if the code has been removed how can we document it?
//  see also @deprecated (Javadoc tag), @ApiStatus.ScheduledForRemoval annotation
// I like this concept, maybe we can make an annotation/javadoc tag for MC version-sensitive functionality
/**
 * Provides brief explanations for code removal for specific project versions.
 *
 * @since 1.0.0
 */
@Documented
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.ANNOTATION_TYPE, ElementType.TYPE_USE, ElementType.LOCAL_VARIABLE})
public @interface Removal {

	/**
	 * @return The developer comment for this example.
	 */
	String because() default "no comment";

	/**
	 * @return
	 */
	String inVersion() default "1.0.0";

}
