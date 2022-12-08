package com.github.sanctum.panther.file;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

class DataTableImpl implements DataTable {
    final Map<String, Object> map = new HashMap<>();

    @Override
    public <T> DataTable set(String key, T value) {
        if (value == null) {
            // TODO: magic string, should be a dedicated object constant
            // that way consumers can choose how to handle nulls
            map.put(key, "NULL");
        } else map.put(key, value);
        return this;
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public @NotNull Map<String, Object> values() {
        return unmodifiableMap(map);
    }
}
