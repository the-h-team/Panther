package com.github.sanctum.panther.file;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

// TODO: Decide if this interface is allowed for extension by users
/**
 * An interface dedicated to writing keyed objects to a {@link Configurable.Editor}
 *
 * @author Hempfest
 * @version 1.0
 */
public interface DataTable {
	/**
	 * Used to represent null values.
	 */
	Object NULL = new Object();

	<T> DataTable set(String key, T value);

	void clear();

	// TODO doc as read-only map (view)
	@NotNull Map<String, Object> values();

	static DataTable newTable() {
		return new DataTableImpl();
	}

}
