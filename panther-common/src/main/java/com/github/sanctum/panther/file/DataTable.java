package com.github.sanctum.panther.file;

import java.util.HashMap;
import java.util.Map;

/**
 * An interface dedicated to writing keyed objects to a {@link Configurable.Editor}
 *
 * @author Hempfest
 * @version 1.0
 */
public interface DataTable {

	<T> DataTable set(String key, T value);

	void clear();

	Map<String, Object> values();

	static DataTable newTable() {
		return new DataTable() {
			final Map<String, Object> map = new HashMap<>();

			@Override
			public <T> DataTable set(String key, T value) {
				if (value == null) {
					map.put(key, "NULL");
				} else map.put(key, value);
				return this;
			}

			@Override
			public void clear() {
				map.clear();
			}

			@Override
			public Map<String, Object> values() {
				return map;
			}
		};
	}

}
