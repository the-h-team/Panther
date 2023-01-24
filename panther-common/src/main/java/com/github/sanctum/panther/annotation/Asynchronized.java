package com.github.sanctum.panther.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks areas with implicit asynchronous requirements.
 *
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME) // TODO why runtime if no reflection use?
@Target(value = {ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.ANNOTATION_TYPE, ElementType.TYPE_USE, ElementType.LOCAL_VARIABLE})
public @interface Asynchronized {}
