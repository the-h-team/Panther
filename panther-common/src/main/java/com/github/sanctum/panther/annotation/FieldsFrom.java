package com.github.sanctum.panther.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jetbrains.annotations.NotNull;

/**
 * For areas that require or utilize field constants from a specific class, use this annotation to point to it.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.TYPE_USE})
public @interface FieldsFrom {

	/**
	 * The class containing the desired fields.
	 *
	 * @return the class containing fields.
	 */
	@NotNull Class<?> value();

}
