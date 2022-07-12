package com.github.sanctum.panther.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>A developer comment. Non documented.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.ANNOTATION_TYPE, ElementType.TYPE_USE})
public @interface Comment {

	/**
	 * @return The developer comment for this example.
	 */
	String value() default "no comment";

	/**
	 * @return
	 */
	String author() default "N/A";

}
