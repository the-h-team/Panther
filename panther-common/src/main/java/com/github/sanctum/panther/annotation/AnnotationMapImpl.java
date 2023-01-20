package com.github.sanctum.panther.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AnnotationMapImpl<T> implements AnnotationMap<T> {

    final T subject;
    final Map<Class<?>, Node> annotationByType = new HashMap<>();
    final Map<Method, Map<Class<? extends Annotation>, List<AnnotationEntryImpl<?>>>> annotationsByMethod = new HashMap<>();
    final Map<Class<?>, List<AnnotationEntry<?>>> annotationFromClass;
    final List<Method> allAnnotatedMethods = new LinkedList<>();


    public AnnotationMapImpl(T subject) {
        this.subject = subject;
        for (Method method : subject.getClass().getMethods()) {
            insert(method);
        }
        annotationFromClass = Arrays.stream(subject.getClass().getAnnotations())
                .map(this::create)
                .collect(Collectors.groupingBy(aD -> aD.getAnnotation().annotationType(), Collectors.toList()));
    }

    private <A extends Annotation> AnnotationEntry<A> create(A annotation) {
        return new AnnotationEntryImpl<>(annotation, null, true);
    }

    private void insert(Method method) {
        Annotation[] annotations = method.getAnnotations();
        if (annotations.length == 0) {
            return;
        }
        allAnnotatedMethods.add(method);
        annotationsByMethod.put(method, Arrays.stream(annotations)
                .map(a -> new AnnotationEntryImpl<>(a, method, method.isAccessible()))
                .peek(a -> {
                    Class<? extends Annotation> aclass = a.getAnnotation().getClass();
                    Node node = annotationByType.computeIfAbsent(aclass, c -> new Node(aclass));
                    node.data.add(a);
                })
                .collect(Collectors.groupingBy(a -> a.getAnnotation().annotationType(), Collectors.toList())));
    }


    @Override
    public <A extends Annotation> Stream<Method> getAllAnnotatedMethods(Class<A> annotationClass, boolean accessibleOnly) {
        if (annotationClass == Annotation.class) {
            return getAllAnnotatedMethods(accessibleOnly);
        }
        Node container = annotationByType.get(annotationClass);
        if (container == null) {
            return Stream.empty();
        }
        Stream<AnnotationEntryImpl<?>> raw = container.data.stream();
        if (accessibleOnly) {
            raw = raw.filter(AnnotationEntryImpl::isAccessible);
        }
        return raw.map(AnnotationEntryImpl::getMethod);
    }

    @Override
    public Stream<Method> getAllAnnotatedMethods(boolean accessibleOnly) {
        return accessibleOnly ?
                allAnnotatedMethods.stream().filter(AccessibleObject::isAccessible) :
                allAnnotatedMethods.stream();
    }


    @Override
    public Stream<Annotation> getAllClassAnnotations() {
        return annotationFromClass.values().stream().flatMap(Collection::stream).map(AnnotationEntry::getAnnotation);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A extends Annotation> Stream<A> getAllClassAnnotations(Class<A> annotationClass) {
        if (annotationClass == Annotation.class) {
            return (Stream<A>) getAllClassAnnotations();
        }
        return annotationFromClass.getOrDefault(annotationClass, Collections.emptyList())
                .stream()
                .map(AnnotationEntry::getAnnotation)
                .map(annotationClass::cast);
    }

    @Override
    public T getSubject() {
        return subject;
    }

    @Override
    public <A extends Annotation> TypeViewImpl<A, T> createView(Class<A> annotationType) {
        return new TypeViewImpl<>(annotationType, this);
    }

    static class Node implements Iterable<AnnotationEntryImpl<?>> {

        final List<AnnotationEntryImpl<?>> data = new LinkedList<>();
        final Class<? extends Annotation> annotationType;

        Node(Class<? extends Annotation> annotationType) {
            this.annotationType = annotationType;
        }

        @NotNull
        public Class<? extends Annotation> getAnnotationType() {
            return annotationType;
        }

        @NotNull
        @Override
        public Iterator<AnnotationEntryImpl<?>> iterator() {
            return data.iterator();
        }
    }

    static class AnnotationEntryImpl<A extends Annotation> implements AnnotationEntry<A> {

        final A annotation;
        final Method method;
        final boolean accessible;

        AnnotationEntryImpl(A annotation, Method method, boolean accessible) {
            this.annotation = annotation;
            this.method = method;
            this.accessible = accessible;
        }

        public A getAnnotation() {
            return annotation;
        }

        public Method getMethod() {
            return method;
        }

        public boolean isAccessible() {
            return accessible;
        }

    }

    private static class TypeViewImpl<A extends Annotation, T> implements TypeView<A, T> {
        final Class<A> annotationType;
        final AnnotationMapImpl<T> map;

        private TypeViewImpl(Class<A> annotationType, AnnotationMapImpl<T> map) {
            this.annotationType = annotationType;
            this.map = map;
        }

        @SuppressWarnings("unchecked")
        public Stream<AnnotationEntry<A>> annotations(Method method) {
            return map.annotationsByMethod.get(method).get(annotationType).stream()
                    .map(a -> (AnnotationEntry<A>) a);
        }

        public <C extends Collection<AnnotationEntry<A>>> C annotations(Method method, Supplier<C> collectionSupplier) {
            return collect(annotations(method), collectionSupplier);
        }

        public Set<AnnotationEntry<A>> annotationSet(Method method) {
            return annotations(method, HashSet::new);
        }

        @SuppressWarnings("unchecked")
        public Stream<AnnotationEntry<A>> fromClass() {
            return map.annotationFromClass.get(annotationType).stream().map(a -> (AnnotationEntry<A>) a);
        }

        public <C extends Collection<AnnotationEntry<A>>> C fromClass(Supplier<C> collectionSupplier) {
            return collect(fromClass(), collectionSupplier);
        }

        public Set<AnnotationEntry<A>> fromClassSet() {
            return fromClass(HashSet::new);
        }

        private <C extends Collection<AnnotationEntry<A>>> C collect(Stream<AnnotationEntry<A>> str, Supplier<C> cSup) {
            return str.collect(Collectors.toCollection(cSup));
        }

        public Stream<Method> methodStream() {
            return map.annotationByType.get(annotationType).data.stream().map(AnnotationEntryImpl::getMethod);
        }

        public boolean isClassAnnotated() {
            return map.annotationFromClass.containsKey(annotationType);
        }

        public boolean isMethodAnnotated() {
            return map.annotationByType.containsKey(annotationType);
        }

        @NotNull
        @Override
        public Iterator<Method> iterator() {
            return methodStream().iterator();
        }


    }

}
