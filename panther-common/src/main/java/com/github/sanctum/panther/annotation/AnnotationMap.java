package com.github.sanctum.panther.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Represents a Read-Only-Map for Annotations discovered on a subject.
 * <p>
 * This will scan methods and the class itself.
 * If you only need to check for a certain annotation type, consider using the more lightweight {@link AnnotationReader}
 */
public interface AnnotationMap<T> {

    /**
     * Provides the scan results for a given annotation type.
     * Ignores accessibility if desired.
     *
     * @param annotationClass the annotation class to get the scan results for
     * @param accessibleOnly  whether to only take accessible methods into account
     * @param <A>             the annotation type to get the scan results for
     * @return a stream containing all found methods. May be empty
     */
    <A extends Annotation> Stream<Method> getAllAnnotatedMethods(Class<A> annotationClass, boolean accessibleOnly);

    /**
     * Provides the scan results for a given annotation class.
     * Only outputs accessible methods
     *
     * @param annotationClass the annotation type to get the scan results for
     * @return a stream containing all methods found. May be empty
     */
    default <A extends Annotation> Stream<Method> getAllAnnotatedMethods(Class<A> annotationClass) {
        return getAllAnnotatedMethods(annotationClass, true);
    }

    /**
     * @param accessibleOnly whether to only take accessible methods into account
     * @return a Stream containing all methods found
     */
    Stream<Method> getAllAnnotatedMethods(boolean accessibleOnly);

    /**
     * @return all methods that are annotated somehow. Ignores accessibility
     */
    default Stream<Method> getAllAnnotatedMethods() {
        return getAllAnnotatedMethods(false);
    }

    /**
     * @return all annotations present on the subject class
     */
    Stream<Annotation> getAllClassAnnotations();

    /**
     * Provides the scan results for a given annotation class.
     * Ignores accessibility if desired.
     *
     * @param annotationClass the annotation type to get the scan results for
     * @param <A>             the annotation type to get the scan results for
     * @return a stream containing all found methods. May be empty
     */
    <A extends Annotation> Stream<A> getAllClassAnnotations(Class<A> annotationClass);

    /**
     * @return the object backing this map.
     */
    T getSubject();

    /**
     * Creates a TypeView for a desired class, providing easier access to annotations of a desired class.
     *
     * @param annotationType the desired annotation class
     * @param <A>            the type of the desired annotation class
     * @return a new TypeView instance
     */
    <A extends Annotation> TypeView<A, T> createView(Class<A> annotationType);

    static <T> AnnotationMap<T> of(T t) {
        return new AnnotationMapImpl<>(t);
    }

    /**
     * Represents an immutable entry in the AnnotationMap.
     * It holds a pair of a method and one of its annotations.
     * In addition to that, it has the information whether this method is accessible or not.
     *
     * @param <A> the annotation type the entry is holding
     */
    interface AnnotationEntry<A extends Annotation> {

        /**
         * @return the annotation held by this entry.
         */
        A getAnnotation();

        /**
         * @return the method held by this entry
         */
        Method getMethod();

        /**
         * @return whether the contained method is accessible or not
         */
        boolean isAccessible();

    }

    /**
     * Represents a view of scan results limited to a given annotation type.
     * It is backed by its creating AnnotationMap
     *
     * @param <A> the annotation type
     * @param <T> the type of the backing annotation map
     */
    interface TypeView<A extends Annotation, T> extends Iterable<Method> {

        /**
         * Provides a Stream of scan results for a given method.
         * It will only contain the found annotations of the type {@link A}
         *
         * @param method the method to retrieve the results for
         * @return the result stream
         */
        Stream<AnnotationEntry<A>> annotations(Method method);

        /**
         * Provides a collection of scan results for a given method.
         * It will only contain the found annotations of the type {@link A}
         *
         * @param method             the method to retrieve the results for
         * @param collectionSupplier the supplier to use to build the result collection
         * @param <C>                the collection type
         * @return the result stream
         */
        <C extends Collection<AnnotationEntry<A>>> C annotations(Method method, Supplier<C> collectionSupplier);

        /**
         * Provides a set of scan results for a given method.
         * It will only contain the found annotations of the type {@link A}
         *
         * @param method the method to retrieve the results for
         * @return the result stream
         */
        Set<AnnotationEntry<A>> annotationSet(Method method);

        /**
         * Provides a stream of scan results on the class of the subject type {@link T}.
         * It will only contain the found annotations of the type {@link A}
         *
         * @return the result stream
         */
        Stream<AnnotationEntry<A>> fromClass();

        /**
         * Provides a collection of scan results on the class of the subject type {@link T}.
         * It will only contain the found annotations of the type {@link A}
         *
         * @param collectionSupplier the supplier to use to build the result collection
         * @param <C>                the collection type
         * @return the result stream
         */
        <C extends Collection<AnnotationEntry<A>>> C fromClass(Supplier<C> collectionSupplier);

        /**
         * Provides a set of scan results on the class of the subject type {@link T}.
         * It will only contain the found annotations of the type {@link A}
         *
         * @return the result stream
         */
        Set<AnnotationEntry<A>> fromClassSet();

        /**
         * @return a stream containing the scan results for all methods annotated with the type {@link A}
         */
        Stream<Method> methodStream();

        /**
         * Checks whether the annotation is present on the subject class
         *
         * @return true when the annotation is present
         */
        boolean isClassAnnotated();

        /**
         * Checks whether the annotation is present on any method of the subject class
         *
         * @return true when the annotation is present
         */
        boolean isMethodAnnotated();

    }

}
