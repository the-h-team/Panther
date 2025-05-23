package com.github.sanctum.panther.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Annotation access wrapper for any type of annotated object and annotation.
 * It discovers target annotations from all methods of a class.
 *
 * @param <T> A type of annotation.
 * @param <R> The class type to be checked for annotations.
 * @deprecated The class has been replaced by {@link AnnotationReader} and {@link AnnotationMap}.
 * Please use them for any new implementation.
 */
@Deprecated
public final class AnnotationDiscovery<T extends Annotation, R> implements Iterable<Method>, AnnotationReader<T, R> {

    private final int count;
    private final Class<T> annotation;
    private final R r;
    private final Class<R> rClass;
    private Set<Method> methods = new HashSet<>();

    AnnotationDiscovery(Class<T> annotation, R r) {
        this.annotation = annotation;
        this.r = r;
        this.rClass = (Class<R>) r.getClass();
        int annotated = 0;

        for (Method method : rClass.getDeclaredMethods()) {
            try {
                method.setAccessible(true);
            } catch (Exception ignored) {
            }
            if (method.isAnnotationPresent(annotation)) {
                annotated++;
            }
        }
        this.count = annotated;

    }

    AnnotationDiscovery(Class<T> annotation, Class<R> r) {
        this.annotation = annotation;
        this.r = null;
        this.rClass = r;
        int annotated = 0;

        for (Method method : rClass.getDeclaredMethods()) {
            try {
                method.setAccessible(true);
            } catch (Exception ignored) {
            }
            if (method.isAnnotationPresent(annotation)) {
                annotated++;
            }
        }
        this.count = annotated;

    }

    public static @NotNull <T extends Annotation, R> AnnotationDiscovery<T, R> of(@NotNull Class<T> c, @NotNull R listener) {
        return new AnnotationDiscovery<>(c, listener);
    }

    public static @NotNull <T extends Annotation, R> AnnotationDiscovery<T, R> of(@NotNull Class<T> c, @NotNull Class<R> listener) {
        return new AnnotationDiscovery<>(c, listener);
    }

    @Override
    public void reset() {
        getFilteredMethods().clear();
    }

    /**
     * Filters the methods and only work with ones of interest.
     * <p>
     * ~~WARNING~~ Overwrites the method collection and doesn't test accessibility, use {@link AnnotationDiscovery#filter(Predicate)} first.
     *
     * @param comparator The comparator to use.
     * @return The same annotation discovery object.
     */
    public AnnotationDiscovery<T, R> sort(Comparator<? super Method> comparator) {
        this.methods = methods.stream().sorted(comparator).collect(Collectors.toCollection(LinkedHashSet::new));
        return this;
    }

    /**
     * Filter the methods and only work with ones of interest.
     *
     * @param hard whether to breach accessibility.
     * @return The same annotation discovery object.
     */
    public AnnotationDiscovery<T, R> filter(boolean hard) {
        return filter(method -> true, hard);
    }

    /**
     * Filter the methods and only work with ones of interest.
     *
     * @param predicate The filtration.
     * @return The same annotation discovery object.
     */
    public AnnotationDiscovery<T, R> filter(Predicate<? super Method> predicate) {
        return filter(predicate, false);
    }

    /**
     * Filter the methods and only work with ones of interest.
     *
     * @param predicate The filtration.
     * @param hard      whether or not to breach accessibility.
     * @return The same annotation discovery object.
     */
    public AnnotationDiscovery<T, R> filter(Predicate<? super Method> predicate, boolean hard) {
        if (!hard) {
            if (methods.isEmpty()) {
                methods.addAll(Arrays.stream(this.rClass.getDeclaredMethods()).filter(m -> m.isAnnotationPresent(annotation) && predicate.test(m)).collect(Collectors.toList()));
            }
        } else {
            if (methods.isEmpty()) {
                methods.addAll(Arrays.stream(this.rClass.getDeclaredMethods()).filter(m -> {
                    try {
                        m.setAccessible(true);
                    } catch (Exception ignored) {
                    }
                    return m.isAnnotationPresent(annotation) && predicate.test(m);
                }).collect(Collectors.toList()));
            }
        }
        return this;
    }

    /**
     * @return true if the desired annotation is present at all.
     */
    public boolean isPresent() {
        return methods.isEmpty() ? this.rClass.isAnnotationPresent(annotation) : count > 0;
    }

    @Override
    public boolean isClassAnnotated() {
        return false;
    }

    @Override
    public boolean isMethodAnnotated() {
        return count > 0;
    }

    @Override
    public boolean hasFilteredMethods() {
        return !methods.isEmpty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void forEachFilteredMethod(BiConsumer<? super T, Method> function) {
        if (isPresent()) {
            methods.forEach(m -> {
                for (Annotation a : m.getAnnotations()) {
                    if (annotation.isAssignableFrom(a.annotationType())) {
                        function.accept((T) a, m);
                    }
                }
            });
        }
    }

    /**
     * Get information from the leading source objects located annotation.
     * <p>
     * This method gives you access to an annotation and the source object itself.
     *
     * @param <U>      The desired return value.
     * @param function The function.
     * @return A value from an annotation.
     */
    public <U> U mapFromClass(AnnotationProcessor<T, R, U> function) {
        if (isPresent()) {
            return function.apply(rClass.getAnnotation(annotation), r);
        }
        return null;
    }

    @Override
    public <R1> R1 mapFromClass(Function<T, R1> function) {
        return null;
    }

    /**
     * Get information from the leading source objects methods found with the specified annotation.
     * <p>
     * This method gives you access to an annotation and the source object itself.
     *
     * @param <U>      The desired return value.
     * @param function The function.
     * @return A value from an annotation.
     */
    public <U> List<U> mapFromMethods(AnnotationProcessor<T, R, U> function) {
        List<U> list = new ArrayList<>();
        ifPresent((t, method) -> list.add(function.apply(t, r)));
        return list;
    }

    public Set<Method> getFilteredMethods() {
        return methods;
    }

    /**
     * Read all annotations from a method that fit this query.
     *
     * @param m The method to read.
     * @return A set of annotations only matching this discovery query.
     */
    public Set<T> read(Method m) {
        return Arrays.stream(m.getAnnotations()).filter(a -> annotation.isAssignableFrom(a.getClass())).map(a -> (T) a).collect(Collectors.toSet());
    }

    /**
     * @return The total amount of relevant annotated methods found.
     */
    public int count() {
        return count;
    }

    @Override
    public int filterCount() {
        return methods.size();
    }

    /**
     * Run an operation for each relative method found.
     *
     * @param consumer The method function.
     */
    @Override
    public void forEach(Consumer<? super Method> consumer) {
        if (methods.isEmpty()) {
            filter(method -> true).methods.forEach(consumer);
        } else {
            methods.forEach(consumer);
        }
    }

    @NotNull
    @Override
    public Iterator<Method> iterator() {
        return getFilteredMethods().iterator();
    }

    @Override
    public Spliterator<Method> spliterator() {
        return getFilteredMethods().spliterator();
    }

    /**
     * Mirror of {@link AnnotationProcessor}
     * <p>
     * Remove usages asap, the support for this will be discontinued soon
     *
     * @deprecated Only for compatibility reasons.
     */
    @Deprecated
    public interface AnnotativeConsumer<U extends Annotation, R, V> extends AnnotationProcessor<U, R, V> {

    }

}
