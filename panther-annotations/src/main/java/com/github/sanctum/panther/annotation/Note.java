package com.github.sanctum.panther.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//FIXME narrow scope to specific usages
// by this i mean split this into multiple annotations of greater semantic value each with appropriately narrow target contexts
//TODO remove if we no longer use this
/**
 * <p>Where javadoc sometimes fails provide safe and easy usage examples or explanations to most object types for IDE's</p>
 */
@Documented
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.ANNOTATION_TYPE, ElementType.TYPE_USE})
public @interface Note {

	/**
	 * @return The developer comment for this example.
	 */
	String value() default "no comment";

}
