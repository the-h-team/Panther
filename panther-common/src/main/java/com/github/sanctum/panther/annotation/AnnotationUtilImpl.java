package com.github.sanctum.panther.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// TODO package-private? (remove public modifier)
// FIXME pull methods up to superinterface
public class AnnotationUtilImpl<A extends Annotation, S> implements AnnotationUtil<A, S> {

    private final int count;
    private final Class<A> annotationType;
    private final S subject;
    private final Class<?> sClass;
    private Set<Method> methodBuffer;
    private Stream<Method> pipeline;
    boolean evaluated = true;
    private final Set<Method> allAnnotatedMethods;

    // TODO package-private? (remove public modifier). rationale: already exposed via AnnotationUtil.of(Class, Object)
    @SuppressWarnings("unchecked")
    public AnnotationUtilImpl(Class<A> annotationType, S subject) {
        this(annotationType, (Class<S>) subject.getClass(), subject);
    }

    AnnotationUtilImpl(Class<A> annotationType, Class<S> sClass) {
        this(annotationType, sClass, null);
    }

    //Combined constructor for both approaches
    AnnotationUtilImpl(Class<A> annotationType, Class<S> sClass, S subject) {
        this.annotationType = annotationType;
        this.sClass = sClass;
        this.subject = subject;

        Set<Method> all = new HashSet<>();
        for (Method method : sClass.getDeclaredMethods()) {
            try {
                method.setAccessible(true);
            } catch (Exception ignored) {
            }
            if (method.isAnnotationPresent(this.annotationType)) {
                all.add(method);
            }
        }
        this.count = all.size();
        allAnnotatedMethods = Collections.unmodifiableSet(all);
        methodBuffer = new HashSet<>(allAnnotatedMethods);
        pipeline = methodBuffer.stream();
    }

    private boolean isEvaluated() {
        return evaluated;
    }

    private void evaluate() {
        if (isEvaluated()) {
            return;
        }
        methodBuffer = pipeline.collect(Collectors.toCollection(LinkedHashSet::new));
        evaluated = true;
        pipeline = methodBuffer.stream();
    }

    public void reset() {
        methodBuffer = new HashSet<>(allAnnotatedMethods);
        pipeline = methodBuffer.stream();
        evaluated = true;
    }

    @Override
    public AnnotationUtil<A, S> sort(Comparator<? super Method> comparator) {
        evaluated = false;
        pipeline = pipeline.sorted(comparator);
        return this;
    }

    /*FIXME: The hard parameter is unnecessary in current construct, check whether it is still wanted.
     * If so, the total collection needs to be changed into a Map<Boolean,Method> for being able to get accessible methods easily
     */
    @Override
    public AnnotationUtil<A, S> filter(Predicate<? super Method> predicate, boolean hard) {
        evaluated = false;
        pipeline = pipeline.filter(predicate);
        return this;
    }


    @Override
    public boolean isPresent() {
        return isClassAnnotated() || isMethodAnnotated();
    }

    //FIXME Candidate for superinterface
    public boolean isClassAnnotated() {
        return sClass.isAnnotationPresent(annotationType);
    }

    //FIXME Candidate for superinterface
    public boolean isMethodAnnotated() {
        return !allAnnotatedMethods.isEmpty();
    }

    //FIXME Candidate for superinterface
    public boolean hasFilteredMethods() {
        return !methodBuffer.isEmpty();
    }

    @Override
    public void ifPresent(BiConsumer<A, Method> function) {
        evaluate();
        methodBuffer.forEach(method -> {
            for (Annotation annotation : method.getAnnotations()) {
                if (annotationType.isAssignableFrom(annotation.annotationType())) {
                    function.accept(annotationType.cast(annotation), method);
                }
            }
        });
    }

    //FIXME: Check whether S is necessary to be included for the consumer or not
    @Override
    public <U> U mapFromClass(AnnotationDiscovery.AnnotativeConsumer<A, S, U> function) {
        if (isClassAnnotated()) {
            return function.accept(sClass.getAnnotation(annotationType), subject);
        }
        return null;
    }

    //FIXME Candidate for superinterface
    public <U> U mapFromClass(Function<A, U> function) {
        if (isClassAnnotated()) {
            return function.apply(sClass.getAnnotation(annotationType));
        }
        return null;
    }

    //FIXME Empty set more desirable than null?
    @Override
    public <U> List<U> mapFromMethods(AnnotationDiscovery.AnnotativeConsumer<A, S, U> function) {
        if (hasFilteredMethods()) {
            return methodBuffer.stream()
                    .map(m -> function.accept(m.getAnnotation(annotationType), subject))
                    .collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public Set<Method> methods() {
        evaluate();
        return methodBuffer;
    }

    @Override
    public Set<A> read(Method m) {
        return read(m, annotationType);
    }

    //FIXME Candidate for superinterface
    public static <A extends Annotation> Set<A> read(Method m, Class<A> aClass) {
        return Arrays.stream(m.getAnnotations())
                .filter(a -> aClass.isAssignableFrom(a.annotationType()))
                .map(aClass::cast)
                .collect(Collectors.toSet()); //TODO
    }

    @Override
    public int count() {
        return count;

    }

    public int filterCount() {
        evaluate();
        return methodBuffer.size();
    }

    @NotNull
    @Override
    public Iterator<Method> iterator() {
        evaluate();
        return methodBuffer.iterator();
    }

    @Override
    public Spliterator<Method> spliterator() {
        return methodBuffer.spliterator();
    }

}
