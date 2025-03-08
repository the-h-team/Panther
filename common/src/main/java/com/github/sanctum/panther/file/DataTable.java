package com.github.sanctum.panther.file;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

// TODO: Decide if this interface is allowed for extension by users
/**
 * An interface dedicated to writing keyed objects to a {@link Configurable.Editor}
 *
 * @since 1.0.2
 * @author Hempfest
 * @version 1.0
 */
public interface DataTable {
	/**
	 * Represents values assigned as {@code null}.
	 */
	Object NULL = new Object();

	/**
	 * Sets the value for a given key.
	 *
	 * @param key the key to set
	 * @param value the value to assign to the key
	 * @return this table
	 * @implSpec Implementations must assign {@link #NULL} for {@code key}
	 * if {@code value} is {@code null}.
	 */
	<T> DataTable set(@NotNull String key, T value);

	/**
	 * Clears the data from this table.
	 */
	void clear();

	/**
	 * Gets a read-only view of this data table.
	 *
	 * @return a read-only map view of this data table
	 */
	@NotNull Map<String, Object> values();

	/**
	 * Creates a new data table.
	 *
	 * @return a new data table
	 */
	static DataTable newTable() {
		return new DataTableImpl();
	}

}
