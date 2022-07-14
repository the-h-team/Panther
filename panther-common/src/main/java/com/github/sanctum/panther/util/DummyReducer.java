package com.github.sanctum.panther.util;

import com.github.sanctum.panther.annotation.Json;

public class DummyReducer implements Json.Reducer {

	@Override
	public Object reduce(Object t) {
		return null;
	}
}
