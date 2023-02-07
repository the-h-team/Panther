package com.github.sanctum.panther.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

// rename class to reflect Method-specific utility?
// It also holds class-specific code, see #mapFromClass etc.
//I think the current name is fine tho. rigob.

/**
 * Processes annotations on the methods of a class.
 * <p>Annotations of a given supertype are processed. The results can be
 * filtered and sorted.
 *
 * @param <A> the annotation supertype to process
 * @param <S> the static type of the class being processed
 */
public interface AnnotationReader<A extends Annotation, S> extends Iterable<Method> {

    /**
     * Applies sorting to the method buffer.
     * <p>This is lazily evaluated.
     *
     * @param comparator the comparator to use
     * @return this annotation util
     */
    AnnotationReader<A, S> sort(Comparator<? super Method> comparator);

    /**
     * Applies a filter to the method buffer.
     * <p>
     * This is lazily evaluated.
     *
     * @param hard whether to breach accessibility
     * @return this annotation util
     * @deprecated the hard parameter is present for compatibility reasons only.
     * The annotation check now <b>always</b> takes all methods into account.
     * Preferably use {@link #filter(Predicate)}, as this feature may be discontinued in the future!
     */
    default AnnotationReader<A, S> filter(boolean hard) {
        return filter(method -> true, hard);
    }

    /**
     * Applies a filter to the method buffer.
     * <p>
     * This is lazily evaluated.
     *
     * @param predicate a test function
     * @return this annotation util
     */
    default AnnotationReader<A, S> filter(Predicate<? super Method> predicate) {
        return filter(predicate, false);
    }

    /**
     * Applies a filter to the method buffer.
     * <p>
     * This is lazily evaluated.
     *
     * @param predicate a test function
     * @param hard      whether to breach accessibility
     * @return this annotation util
     * @deprecated the hard parameter is present for compatibility reasons only.
     * The annotation check now <b>always</b> takes all methods into account.
     * Preferably use {@link #filter(Predicate)}, as this feature may be discontinued in the future!
     */
    AnnotationReader<A, S> filter(Predicate<? super Method> predicate, boolean hard);


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
     * Checks the cl
     *
     * @param <U>      the desired return type.
     * @param function the processing function.
     * @return the processing results, or null if the annotation wasn't present.
     */
    <U> U mapFromClass(AnnotationProcessor<A, S, U> function);


    /**
     * FIXME remove or re-doc
     * FIXME using deprecated components as parameter, either pull or replace them
     * Get information from the leading source objects methods found with the specified annotation.
     * <p>
     * This method gives you access to an annotation and the source object itself.
     *
     * @param <U>      The desired return value.
     * @param function The function.
     * @return A value from an annotation.
     */
    <U> List<U> mapFromMethods(AnnotationProcessor<A, S, U> function);

    /**
     * Resets all filtering and sorting settings.
     */
    void reset();

    /**
     * Provides the currently buffered methods.
     *
     * @return a set of methods. If a comparator was provided by {@link #sort(Comparator),
     * the set may be ordered at least statically.
     */
    Set<Method> getFilteredMethods();


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
        return AnnotationReaderImpl.read(method, aClass);
    }

    /**
     * Factory method for creating AnnotationReader instances by subject object.
     * <p>
     * This uses a backing object which will be used for further processing.
     *
     * @param annotationClass the annotation class to check for
     * @param subject         the object to be checked
     * @param <A>             the annotation type
     * @param <S>             the type of the object to be checked
     * @return a new AnnotationReader instance.
     */
    static @NotNull <A extends Annotation, S> AnnotationReader<A, S> of(@NotNull Class<A> annotationClass, @NotNull S subject) {
        return new AnnotationReaderImpl<>(annotationClass, subject);
    }

    /**
     * Factory method for creating AnnotationReader instances by subject class.
     * <p>
     * This uses no backing object. <br>
     * WARNING: Refrain from trying to access the subject in
     * {@link AnnotationProcessor AnnotationProcessors} that you use with the provided instance, as it will be null!
     *
     * @param annotationClass the annotation class to check for
     * @param <S>             the type of the object to be checked
     * @return a new AnnotationReader instance.
     */
    static @NotNull <T extends Annotation, S> AnnotationReader<T, S> of(@NotNull Class<T> annotationClass, @NotNull Class<S> sClass) {
        return new AnnotationReaderImpl<>(annotationClass, sClass);
    }

}
