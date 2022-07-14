package com.github.sanctum.panther.container;

public final class PantherArrays {

	public static <T> PantherCollection<T> asList(T... t) {
		PantherCollection<T> collection = new PantherList<>();
		for (T tt : t) {
			collection.add(tt);
		}
		return collection;
	}

}
