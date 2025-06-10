package com.github.sanctum.panther.util;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Supplier;

public abstract class EasyTypeAdapter<T> implements TypeAdapter<T>, Supplier<Type> {

	private final Type type;

	public EasyTypeAdapter() {
		type = resolveType();
	}

	@Override
	public final Type get() {
		return type;
	}

	@Override
	public final boolean equals(final Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}
		final EasyTypeAdapter<?> that = (EasyTypeAdapter<?>) o;
		return type.equals(that.type);
	}

	@Override
	public final int hashCode() {
		return type.hashCode();
	}

	@Override
	public final String toString() {
		return type.toString();
	}

	private Type resolveType() {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		final Class<EasyTypeAdapter<T>> superclass = (Class) EasyTypeAdapter.class;
		@SuppressWarnings("unchecked")
		final Class<? extends EasyTypeAdapter<T>> thisClass = (Class<EasyTypeAdapter<T>>) getClass();
		final Class<?> actualSuperclass = thisClass.getSuperclass();
		if ( actualSuperclass != superclass ) {
			throw new IllegalArgumentException(thisClass + " must extend " + superclass + " directly but it extends " + actualSuperclass);
		}
		final Type genericSuperclass = thisClass.getGenericSuperclass();
		if ( !(genericSuperclass instanceof ParameterizedType) ) {
			throw new IllegalArgumentException(thisClass + " must parameterize its superclass " + genericSuperclass);
		}
		final ParameterizedType parameterizedGenericSuperclass = (ParameterizedType) genericSuperclass;
		final Type[] actualTypeArguments = parameterizedGenericSuperclass.getActualTypeArguments();
		if ( actualTypeArguments.length != 1 ) {
			throw new AssertionError(actualTypeArguments.length);
		}
		return actualTypeArguments[0];
	}


	@Override
	public Class<T> getType() {
		return (Class<T>) get();
	}
}
