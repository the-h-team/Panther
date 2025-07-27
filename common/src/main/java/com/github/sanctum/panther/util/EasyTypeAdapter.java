package com.github.sanctum.panther.util;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.function.Supplier;

public abstract class EasyTypeAdapter<T> implements TypeAdapter<T>, Supplier<Type> {

	private final TypeToken<T> type;

	public EasyTypeAdapter() {
		type = new TypeToken<T>(){};
	}

	@Override
	public final Type get() {
		return type.getType();
	}

	@Override
	public Class<T> getType() {
		return (Class<T>) type.getRawType();
	}
}
