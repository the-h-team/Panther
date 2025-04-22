package com.github.sanctum.panther.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A wiring tool.
 * <p>
 * Once applied, methods of an object may be sorted with respect to this
 * annotation's value or presence. This allows for, among other things, total
 * ordering of methods irrespective of their names and order in source code.
 *
 * @since 1.0.0
 * @author Hempfest
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Ordinal {

	/**
	 * An ordinal or index value for ordering elements (methods).
	 *
	 * @return the ordinal of this element
	 */
	int value() default 0;

}
