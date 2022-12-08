package com.github.sanctum.panther.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

//A annotation, S subject
public interface AnnotationUtil<A extends Annotation, S> extends Iterable<Method> {

    /**
     * Filters the methods and only work with ones of interest.
     * <p>
     * ~~WARNING~~ Overwrites the method collection and doesn't test accessibility, use {@link AnnotationDiscovery#filter(Predicate)} first.
     *
     * @param comparator The comparator to use.
     * @return The same annotation discovery object.
     */
    AnnotationUtil<A, S> sort(Comparator<? super Method> comparator);

    /**
     * Filter the methods and only work with ones of interest.
     *
     * @param hard whether to breach accessibility.
     * @return The same annotation discovery object.
     */
    default AnnotationUtil<A, S> filter(boolean hard) {
        return filter(method -> true, hard);
    }

    /**
     * Filter the methods and only work with ones of interest.
     *
     * @param predicate The filtration.
     * @return The same annotation discovery object.
     */
    default AnnotationUtil<A, S> filter(Predicate<? super Method> predicate) {
        return filter(predicate, false);
    }

    /**
     * Filter the methods and only work with ones of interest.
     *
     * @param predicate The filtration.
     * @param hard      whether or not to breach accessibility.
     * @return The same annotation discovery object.
     */
    AnnotationUtil<A, S> filter(Predicate<? super Method> predicate, boolean hard);


    /**
     * @return true if the desired annotation is present at all.
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


    static @NotNull <T extends Annotation, S> AnnotationDiscovery<T, S> of(@NotNull Class<T> c, @NotNull S listener) {
        return new AnnotationDiscovery<>(c, listener);
    }

    static @NotNull <T extends Annotation, S> AnnotationDiscovery<T, S> of(@NotNull Class<T> c, @NotNull Class<S> listener) {
        return new AnnotationDiscovery<>(c, listener);
    }

}
