package com.github.sanctum.panther.util;

import com.github.sanctum.panther.container.PantherCollection;
import com.github.sanctum.panther.container.PantherEntryMap;
import com.github.sanctum.panther.container.PantherMap;
import com.github.sanctum.panther.container.PantherSet;
import com.github.sanctum.panther.recursive.Service;
import com.github.sanctum.panther.recursive.ServiceFactory;
import com.github.sanctum.panther.recursive.ServiceLoader;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MapDecompression implements Service {

	final Obligation obligation = () -> "To allow the unraveling of configurable standard keyed entries within a map.";

	MapDecompression(){}

	@Override
	public @NotNull Obligation getObligation() {
		return obligation;
	}

	private String appendChild(String key, char divider, String text) {
		return key != null ? key + divider + text : text;
	}

	public EntryMapper decompress(@NotNull Iterable<? extends Map.Entry<String, Object>> iterable, char divider) {
		return decompress(iterable, divider, null);
	}

	public Map<String, Object> convert(@NotNull PantherMap<String, Object> map) {
		Map<String, Object> m = new HashMap<>();
		for (Map.Entry<String, Object> entry : map) {
			if (entry.getValue() instanceof PantherMap) {
				m.putAll(convert((PantherMap<String, Object>) entry.getValue()));
			} else {
				if (entry.getValue() instanceof PantherCollection) {
					List<Object> list = new ArrayList<>();
					((PantherCollection<?>)entry.getValue()).forEach(list::add);
					m.put(entry.getKey(), list);
				} else {
					m.put(entry.getKey(), entry.getValue());
				}
			}
		}
		return m;
	}

	public EntryMapper decompress(@NotNull Iterable<? extends Map.Entry<String, Object>> iterable, char divider, @Nullable String parentKey) {
		return new EntryMapper() {
			@Override
			public Set<String> toSet() {
				return decompressNormalKeys(iterable, divider, parentKey);
			}

			@Override
			public PantherCollection<String> toPantherSet() {
				return decompressLabyrinthKeys(iterable, divider, parentKey);
			}

			@Override
			public Map<String, Object> toMap() {
				return decompressNormalValues(iterable, divider, parentKey);
			}

			@Override
			public PantherMap<String, Object> toPantherMap() {
				return decompressLabyrinthValues(iterable, divider, parentKey);
			}
		};
	}

	Set<String> decompressNormalKeys(@NotNull Iterable<? extends Map.Entry<String, Object>> iterable, char divider, @Nullable String parent) {
		Set<String> set = new HashSet<>();
		for (Map.Entry<String, Object> entry : iterable) {
			if (entry.getValue() instanceof Map) {
				set.addAll(decompressNormalKeys(((Map<String, Object>) entry.getValue()).entrySet(), divider, appendChild(parent, divider, entry.getKey())));
			} else {
				if (entry.getValue() instanceof PantherMap) {
					decompressLabyrinthKeys(((PantherMap<String, Object>) entry.getValue()).entries(), divider, appendChild(parent, divider, entry.getKey())).forEach(set::add);
				} else {
					set.add(appendChild(parent, divider, entry.getKey()));
				}
			}
		}
		return set;
	}

	Map<String, Object> decompressNormalValues(@NotNull Iterable<? extends Map.Entry<String, Object>> iterable, char divider, @Nullable String parent) {
		Map<String, Object> m = new HashMap<>();
		for (Map.Entry<String, Object> entry : iterable) {
			if (entry.getValue() instanceof Map) {
				m.putAll(decompressNormalValues(((Map<String, Object>) entry.getValue()).entrySet(), divider, appendChild(parent, divider, entry.getKey())));
			} else {
				if (entry.getValue() instanceof PantherMap) {
					decompressLabyrinthValues(((PantherMap<String, Object>) entry.getValue()).entries(), divider, appendChild(parent, divider, entry.getKey())).forEach(e -> m.put(e.getKey(), e.getValue()));
				} else {
					m.put(appendChild(parent, divider, entry.getKey()), entry.getValue());
				}
			}
		}
		return m;
	}

	PantherCollection<String> decompressLabyrinthKeys(@NotNull Iterable<? extends Map.Entry<String, Object>> iterable, char divider, @Nullable String parent) {
		PantherCollection<String> set = new PantherSet<>();
		for (Map.Entry<String, Object> entry : iterable) {
			if (entry.getValue() instanceof PantherMap) {
				set.addAll(decompressLabyrinthKeys(((PantherMap<String, Object>) entry.getValue()).entries(), divider, appendChild(parent, divider, entry.getKey())));
			} else {
				if (entry.getValue() instanceof Map) {
					set.addAll(decompressNormalKeys(((Map<String, Object>) entry.getValue()).entrySet(), divider, appendChild(parent, divider, entry.getKey())));
				} else {
					set.add(appendChild(parent, divider, entry.getKey()));
				}
			}
		}
		return set;
	}

	PantherMap<String, Object> decompressLabyrinthValues(@NotNull Iterable<? extends Map.Entry<String, Object>> iterable, char divider, @Nullable String parent) {
		PantherMap<String, Object> m = new PantherEntryMap<>();
		for (Map.Entry<String, Object> entry : iterable) {
			if (entry.getValue() instanceof PantherMap) {
				decompressLabyrinthValues(((PantherMap<String, Object>) entry.getValue()).entries(), divider, appendChild(parent, divider, entry.getKey())).forEach(e -> m.put(e.getKey(), e.getValue()));
			} else {
				if (entry.getValue() instanceof Map) {
					decompressNormalValues(((Map<String, Object>) entry.getValue()).entrySet(), divider, appendChild(parent, divider, entry.getKey())).forEach(m::put);
				} else {
					m.put(appendChild(parent, divider, entry.getKey()), entry.getValue());
				}
			}
		}
		return m;
	}

	public static @NotNull MapDecompression getInstance() {
		MapDecompression service = ServiceFactory.getInstance().getService(MapDecompression.class);
		if (service == null) {
			ServiceLoader loader = ServiceFactory.getInstance().newLoader(MapDecompression.class);
			loader.supply(new MapDecompression());
			return loader.load();
		}
		return service;
	}

}
