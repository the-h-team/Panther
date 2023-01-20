package com.github.sanctum.panther.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Representation of a container that holds the annotations of the methods of a class.
 * The scan will be performed for an annotation type given.
 * The results can be filtered and sorted.
 *
 * @param <A> The annotation type the class will be checked for
 * @param <S> the type of the read target
 */
public interface AnnotationUtil<A extends Annotation, S> extends Iterable<Method> {


    /**
     * Sorts the method buffer. This is lazy evaluated.
     *
     * @param comparator The comparator to use
     * @return The same annotation discovery object
     */
    AnnotationUtil<A, S> sort(Comparator<? super Method> comparator);

    /**
     * Filter the methods and only work with ones of interest.
     *
     * @param hard whether to breach accessibility
     * @return The same annotation discovery object
     */
    default AnnotationUtil<A, S> filter(boolean hard) {
        return filter(method -> true, hard);
    }

    /**
     * Filter the methods and only work with ones of interest.
     *
     * @param predicate The filtration
     * @return The same annotation discovery object
     */
    default AnnotationUtil<A, S> filter(Predicate<? super Method> predicate) {
        return filter(predicate, false);
    }

    /**
     * Filter the methods and only work with ones of interest.
     *
     * @param predicate The filtration
     * @param hard      whether to breach accessibility
     * @return The same annotation discovery object
     */
    AnnotationUtil<A, S> filter(Predicate<? super Method> predicate, boolean hard);


    /**
     * @return true when the desired annotation is present at all.
     */
    boolean isPresent();


    /**
     * Run an operation with every annotated method found.
     *
     * @param function The function.
     * @deprecated misleading name, renamed to {@link }TODO
     */
    void ifPresent(BiConsumer<A, Method> function);


    /**
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
     * Resets all the filtering and sorting performed on the discovered methods.
     */
    void reset();

    /**
     * @return List's all filtered methods.
     */
    Set<Method> methods();


    /**
     * Read all annotations from a method that fit this query.
     *
     * @param m The method to read.
     * @return A set of annotations only matching this discovery query.
     */
    Set<A> read(Method m);


    /**
     * @return The total amount of relevant annotated methods found.
     */
    int count();

    /**
     * @return The amount of methods after filtering
     */
    int filterCount();

    /**
     * Reads all the annotations assignable from a certain class from a method.
     *
     * @param m      the method to check for annotations
     * @param aClass the annotation class the read annotations have to be assignable from.
     * @param <A>    the annotation supertype all read annotations will have.
     * @return a set containing all discovered annotations.
     */
    static <A extends Annotation> Set<A> read(Method m, Class<A> aClass) {
        return AnnotationUtilImpl.read(m, aClass);
    }

    static @NotNull <T extends Annotation, S> AnnotationUtil<T, S> of(@NotNull Class<T> c, @NotNull S listener) {
        return new AnnotationUtilImpl<>(c, listener);
    }

    static @NotNull <T extends Annotation, S> AnnotationUtil<T, S> of(@NotNull Class<T> c, @NotNull Class<S> listener) {
        return new AnnotationUtilImpl<>(c, listener);
    }

}
