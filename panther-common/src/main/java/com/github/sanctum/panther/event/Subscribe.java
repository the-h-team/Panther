package com.github.sanctum.panther.event;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Subscribe {

	Vent.Priority priority() default Vent.Priority.MEDIUM;

	boolean processCancelled() default false;

	String[] resultProcessors() default {};

	@FunctionalInterface
	interface Consumer<T extends Vent> {

		void accept(T event, Vent.Subscription<T> subscription);

	}
}
