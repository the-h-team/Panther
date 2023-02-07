package com.github.sanctum.panther.annotation;

import java.lang.annotation.Annotation;

/**
 * Represents a mapping function to be performed with an annotation and its holding object.
 * <p>
 * This is a functional interface whose functional method is {@link #accept(Annotation, Object)}
 *
 * @param <A> The annotation type
 * @param <S> The subject type that holds the annotation
 * @param <R> The result type after mapping
 */
@FunctionalInterface
public interface AnnotationProcessor<A extends Annotation, S, R> {

    /**
     * Processes an annotation and its holding source to a result.
     *
     * @param annotation the annotation
     * @param source     the holding object
     * @return the processing result
     */
    R accept(A annotation, S source);

}
