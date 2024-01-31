package com.github.sanctum.panther.container;

import java.util.Map;
import org.jetbrains.annotations.NotNull;

public interface PantherEntry<K, V> {

	@NotNull K getKey();

	V getValue();

	static <K, V> @NotNull PantherEntry<K, V> of(K k, V v) {
		return new PantherEntry<K, V>() {
			@Override
			public @NotNull K getKey() {
				return k;
			}

			@Override
			public V getValue() {
				return v;
			}

			@Override
			public String toString() {
				return "Entry{key=" + k + ", value=" + v + "}";
			}

		};
	}

	interface Modifiable<K, V> extends PantherEntry<K, V>, Map.Entry<K, V> {

		static <K,V> @NotNull Modifiable<K, V> of(K key, V value) {
			return new Modifiable<K, V>() {

				private final K k;
				private V v;

				{
					this.k = key;
					this.v = value;
				}

				@Override
				public V setValue(V value) {
					return (this.v = value);
				}

				@Override
				public @NotNull K getKey() {
					return this.k;
				}

				@Override
				public V getValue() {
					return this.v;
				}

				@Override
				public String toString() {
					return "Entry{key=" + k + ", value=" + v + "}";
				}
			};
		}

	}
}
