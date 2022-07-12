package com.github.sanctum.panther.container;

import java.util.Map;
import java.util.function.Function;

/**
 * Represents a {@link PantherMap} that cannot be modified only read from.
 *
 * @param <K> The type of map this is.
 */
public abstract class ImmutablePantherMap<K, V> extends PantherMapBase<K, V> {

	ImmutablePantherMap(PantherMap<K, V> map) {
		map.forEach(entry -> addImmutable(entry.getKey(), entry.getValue()));
	}

	ImmutablePantherMap(Map<K, V> map) {
		map.forEach(this::addImmutable);
	}

	void addImmutable(K k, V v) {
		Node storage = new Node(new ImmutablePantherEntry<>(k, v));
		storage.next = null;
		if (head == null) {
			head = storage;
		} else {
			Node last = head;
			while (last.next != null) {
				last = last.next;
			}
			last.next = storage;
		}
		tail = storage;
		size++;
	}

	@Override
	@Deprecated
	public V put(K e, V value) {
		throw warning();
	}

	@Override
	@Deprecated
	public boolean putAll(Iterable<Map.Entry<K, V>> iterable) {
		throw warning();
	}

	@Override
	@Deprecated
	public boolean removeAll(Iterable<Map.Entry<K, V>> iterable) {
		throw warning();
	}

	@Override
	@Deprecated
	public boolean remove(K e) {
		throw warning();
	}

	@Override
	@Deprecated
	public void clear() {
		throw warning();
	}

	@Override
	@Deprecated
	public V computeIfAbsent(K key, V value) {
		throw warning();
	}

	@Override
	@Deprecated
	public V computeIfAbsent(K key, Function<K, V> function) {
		throw warning();
	}

	public static <K, V> ImmutablePantherMap<K, V> of(PantherMap<K, V> map) {
		return new ImmutablePantherMap<K ,V>(map) {
		};
	}

	public static <K, V> ImmutablePantherMap<K, V> of(Map<K, V> map) {
		return new ImmutablePantherMap<K, V>(map) {
		};
	}

	RuntimeException warning() {
		return new ImmutableStorageException("Element modifications cannot be made to immutable maps!");
	}

	public static <K, V> Builder<K, V> builder() {
		return new Builder<>();
	}

	public static final class Builder<K, V> {

		private final PantherMap<K, V> internal;

		Builder() {
			internal = new PantherEntryMap<>();
		}

		public Builder<K, V> put(K key, V value) {
			internal.put(key, value);
			return this;
		}

		public ImmutablePantherMap<K, V> build() {
			return of(internal);
		}

	}


}
