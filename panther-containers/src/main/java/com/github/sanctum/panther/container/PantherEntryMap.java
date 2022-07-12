package com.github.sanctum.panther.container;

import java.util.Map;

public final class PantherEntryMap<K, V> extends PantherMapBase<K, V> {

	public PantherEntryMap() {
		super();
	}

	public PantherEntryMap(int capacity) {
		super(capacity);
	}

	public PantherEntryMap(Iterable<Map.Entry<K, V>> iterable) {
		super(iterable);
	}

	public PantherEntryMap(Iterable<Map.Entry<K, V>> iterable, int capacity) {
		super(iterable, capacity);
	}

	public PantherEntry.Modifiable<K, V> getFirst() {
		if (head == null) return null;
		return head.value;
	}

	public PantherEntry.Modifiable<K, V> getLast() {
		if (tail == null) return null;
		return tail.value;
	}

}
