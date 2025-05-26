package com.github.sanctum.panther.util;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class EasyTypeAdapter<T> implements TypeAdapter<T> {

	private final Type token;

	public EasyTypeAdapter() {
		this.token = new TypeToken<T>(){}.getType();
	}

	@Override
	public Class<T> getType() {
		return (Class<T>) token.getClass();
	}
}
