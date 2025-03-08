package com.github.sanctum.panther.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Detects annotations on a class and its methods and caches them for further processing.
 * <p>
 * The whole set of discovered methods is saved and a modifiable copy can be filtered and sorted arbitrarily.
 * Only Annotations of a given type are taken into account. If you need to access multiple annotation types at once,
 * consider using an {@link AnnotationMap} for extended utility.
 *
 * @param <A> the annotation supertype to process
 * @param <S> the static type of the class being processed
 * @see AnnotationMap
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
     * Determines whether the annotation can be found anywhere in the source.
     *
     * @return true when the desired annotation is present on any method or on the class
     * @see #isClassAnnotated()
     * @see #isMethodAnnotated()
     * @see #hasFilteredMethods()
     * @deprecated This method provides no information where the annotation got found and has been split to more detailed checks
     */
    boolean isPresent();

    /**
     * Checks whether the class holds the annotation.
     *
     * @return true when the class holds the annotation
     */
    boolean isClassAnnotated();

    /**
     * Checks whether any method in the class holds the annotation.
     *
     * @return true when the annotation could be found
     */
    boolean isMethodAnnotated();

    /**
     * Checks whether the current filters have lead to any results.
     *
     * @return true when filtered method collection is not empty.
     */
    boolean hasFilteredMethods();

    /**
     * Runs an operation with every annotated method found.
     *
     * @param function an operation
     * @deprecated misleading name, renamed to {@link #forEachFilteredMethod(BiConsumer)}
     */
    default void ifPresent(BiConsumer<? super A, Method> function) {
        forEachFilteredMethod(function);
    }

    /**
     * Runs an operation with every annotated method found.
     *
     * @param function an operation
     */
    void forEachFilteredMethod(BiConsumer<? super A, Method> function);

    /**
     * Checks the class for the annotation and processes to a result.
     *
     * @param <R>      the desired return type.
     * @param function the processing function.
     * @return the processing results, or null if the annotation wasn't present.
     */
    <R> R mapFromClass(AnnotationProcessor<A, S, R> function);

    /**
     * Checks the class for the annotation and processes to a result.
     *
     * @param <R>      the desired return type.
     * @param function the processing function.
     * @return the processing results, or null if the annotation wasn't present.
     */
    <R> R mapFromClass(Function<A, R> function);

    /**
     * Takes all methods in the filter buffer, extracts the annotations and processes them with the given function.
     * <p>
     * The processing function will get passed the annotation and the holding object for each occurrence.
     * Note that the method itself won't be easily accessible.
     *
     * @param <R>      The desired return value.
     * @param function The function.
     * @return A value from an annotation.
     */
    <R> List<R> mapFromMethods(AnnotationProcessor<A, S, R> function);

    /**
     * Resets all filtering and sorting settings.
     */
    void reset();

    /**
     * Provides the currently buffered methods.
     *
     * @return a set of methods. If a comparator was provided by {@link #sort(Comparator)},
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
