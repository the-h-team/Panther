package com.github.sanctum.panther.util;

import com.github.sanctum.panther.container.PantherCollection;
import com.github.sanctum.panther.container.PantherMap;
import java.util.Map;
import java.util.Set;

public interface EntryMapper {

	Set<String> toSet();

	PantherCollection<String> toPantherSet();

	Map<String, Object> toMap();

	PantherMap<String, Object> toPantherMap();

}
