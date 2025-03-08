package com.github.sanctum.panther.util;

import java.util.Collection;
import java.util.Collections;

class PantherPagination<T> extends AbstractPaginatedCollection<T> {

	PantherPagination() {
		super(Collections.emptyList());
	}

	PantherPagination(Collection<T> collection) {
		super(collection);
	}

	@SafeVarargs
	PantherPagination(T... collection) {
		super(collection);
	}

}
