package com.github.sanctum.panther.util;

import com.google.gson.reflect.TypeToken;

public class EasyTypeAdapter<T> implements TypeAdapter<T> {

	private final TypeToken<T> token;

	public EasyTypeAdapter() {
		this.token = new TypeToken<T>(){};
	}

	@Override
	public Class<T> getType() {
		return (Class<T>) token.getRawType();
	}
}
