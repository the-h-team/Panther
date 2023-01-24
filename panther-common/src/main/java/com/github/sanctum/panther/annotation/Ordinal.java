package com.github.sanctum.panther.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// TODO document or remove. too much magic
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Ordinal {

	int value() default 0;

}
