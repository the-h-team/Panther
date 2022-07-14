package com.github.sanctum.panther.util;

import java.util.List;

public interface OrdinalResult<E> {

	default <R> R cast(TypeAdapter<R> flag) {
		return flag.getType().cast(get().getElement());
	}

	OrdinalElement<E> get();

	List<OrdinalElement<E>> getAll();

	static <E> OrdinalResult<E> of(OrdinalElement<E> e) {
		return new OrdinalResult<E>() {
			@Override
			public OrdinalElement<E> get() {
				return e;
			}

			@Override
			public List<OrdinalElement<E>> getAll() {
				return null;
			}

		};
	}

	static <E, T extends OrdinalElement<E>> OrdinalResult<E> of(List<T> es) {
		return new OrdinalResult<E>() {
			@Override
			public OrdinalElement<E> get() {
				return null;
			}

			@Override
			public List<OrdinalElement<E>> getAll() {
				return (List<OrdinalElement<E>>) es;
			}
		};
	}

}