package com.github.sanctum.panther.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

// TODO rename class to reflect Method-specific utility?
/**
 * Processes annotations on the methods of a class.
 * <p>Annotations of a given supertype are processed. The results can be
 * filtered and sorted.
 *
 * @param <A> the annotation supertype to process
 * @param <S> the static type of the class being processed
 */
public interface AnnotationUtil<A extends Annotation, S> extends Iterable<Method> {

    /**
     * Applies sorting to the method buffer.
     * <p>This is lazily evaluated.
     *
     * @param comparator the comparator to use
     * @return this annotation util
     */
    AnnotationUtil<A, S> sort(Comparator<? super Method> comparator);

    /**
     * Applies a filter to the method buffer.
     * <p>This is lazily evaluated.
     *
     * @param hard whether to breach accessibility FIXME is this still needed? working?
     * @return this annotation util
     */
    default AnnotationUtil<A, S> filter(boolean hard) {
        return filter(method -> true, hard);
    }

    /**
     * Applies a filter to the method buffer.
     * <p>This is lazily evaluated.
     *
     * @param predicate a test function
     * @return this annotation util
     */
    default AnnotationUtil<A, S> filter(Predicate<? super Method> predicate) {
        return filter(predicate, false);
    }

    /**
     * Applies a filter to the method buffer.
     * <p>This is lazily evaluated.
     *
     * @param predicate a test function
     * @param hard      whether to breach accessibility FIXME this is unused in the impl
     * @return this annotation util
     */
    AnnotationUtil<A, S> filter(Predicate<? super Method> predicate, boolean hard);


    /**
     * Determines if any annotated methods are present.
     *
     * @return true if the desired annotation is present on any method
     */
    boolean isPresent();


    /**
     * Runs an operation with every annotated method found.
     *
     * @param function an operation
     * @deprecated misleading name, renamed to {@link }TODO
     */
    void ifPresent(BiConsumer<A, Method> function); // FIXME use ? super A


    /**
     * FIXME remove or re-doc
     * FIXME using deprecated components as parameter, either pull or replace them
     * Get information from the leading source objects located annotation.
     * <p>
     * This method gives you access to an annotation and the source object itself.
     *
     * @param function The function.
     * @param <U>      The desired return value.
     * @return A value from an annotation.
     */
    <U> U mapFromClass(AnnotationDiscovery.AnnotativeConsumer<A, S, U> function);


    /**
     * FIXME remove or re-doc
     * FIXME using deprecated components as parameter, either pull or replace them
     * Get information from the leading source objects methods found with the specified annotation.
     * <p>
     * This method gives you access to an annotation and the source object itself.
     *
     * @param function The function.
     * @param <U>      The desired return value.
     * @return A value from an annotation.
     */
    <U> List<U> mapFromMethods(AnnotationDiscovery.AnnotativeConsumer<A, S, U> function);

    /**
     * Resets all filtering and sorting settings.
     */
    void reset();

    /**
     * Gets all methods after filtering.
     *
     * @return a filtered list of methods
     */
    Set<Method> methods(); // FIXME prefer "getMethods" or "getFilteredMethods"


    /**
     * Reads all matching annotations from a method.
     *
     * @param method the method to scan
     * @return the set of annotations matching this discovery query
     */
    Set<A> read(Method method);


    /**
     * Gets the total number of relevant annotated methods found.
     *
     * @return the total number of relevant methods found
     */
    int count();

    /**
     * Gets the number of methods after filtering.
     *
     * @return the number of methods after filtering
     */
    int filterCount();

    /**
     * Reads all annotations assignable from a certain class from a method.
     * <p>
     * Discovers on {@code method} all annotations which inherit from
     * {@code aClass}.
     *
     * @param method the method to check for annotations
     * @param aClass the class each annotation must be assignable from
     * @param <A>    the supertype of all read annotations
     * @return a set of annotations
     */
    static <A extends Annotation> Set<A> read(Method method, Class<A> aClass) {
        return AnnotationUtilImpl.read(method, aClass);
    }

    // FIXME doc
    static @NotNull <T extends Annotation, S> AnnotationUtil<T, S> of(@NotNull Class<T> annotationClass, @NotNull S subject) {
        return new AnnotationUtilImpl<>(annotationClass, subject);
    }

    // FIXME doc
    static @NotNull <T extends Annotation, S> AnnotationUtil<T, S> of(@NotNull Class<T> annotationClass, @NotNull Class<S> sClass) {
        return new AnnotationUtilImpl<>(annotationClass, sClass);
    }

}
