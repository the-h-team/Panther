package com.github.sanctum.panther.util;

import java.lang.reflect.Type;
import java.util.UUID;

@FunctionalInterface
public interface TypeAdapter<T> extends Type {

	TypeAdapter<UUID> UUID = () -> UUID.class;
	TypeAdapter<String> STRING = () -> String.class;
	TypeAdapter<Boolean> BOOLEAN = () -> Boolean.class;
	TypeAdapter<Number> NUMBER = () -> Number.class;

	Class<T> getType();

	@Override
	default String getTypeName() {
		return getType().getTypeName();
	}

	default T cast(Object o) {
		return (T) o;
	}

	static <T> TypeAdapter<T> get() {
		return new EasyTypeAdapter<>();
	}


}
