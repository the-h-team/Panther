package com.github.sanctum.panther.container;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;

public final class PantherCollectors {

	/**
	 * Get a collector for a new labyrinth list.
	 *
	 * @param <T> The type of list to collect.
	 * @return a fresh labyrinth list collector.
	 */
	public static <T> Collector<T, ?, PantherList<T>> toList() {
		return Collector.of(PantherList::new, PantherCollection::add,
				(left, right) -> {
					left.addAll(right);
					return left;
				});
	}

	/**
	 * Get a collector for a new labyrinth set.
	 *
	 * @param <T> The type of set to collect.
	 * @return a fresh labyrinth set collector.
	 */
	public static <T> Collector<T, ?, PantherSet<T>> toSet() {
		return Collector.of(PantherSet::new, PantherCollection::add,
				(left, right) -> {
					left.addAll(right);
					return left;
				});
	}

	/**
	 * Get a collector for a new immutable labyrinth list.
	 *
	 * @param <T> The type of immutable list to collect.
	 * @return a fresh immutable labyrinth list collector.
	 */
	public static <T> Collector<T, ?, PantherCollection<T>> toImmutableList() {
		return Collector.of(PantherList::new, PantherCollection::add,
				(left, right) -> {
					left.addAll(right);
					return ImmutablePantherCollection.of(left);
				});
	}

	/**
	 * Get a collector for a new immutable labyrinth set.
	 *
	 * @param <T> The type of immutable set to collect.
	 * @return a fresh immutable labyrinth set collector.
	 */
	public static <T> Collector<T, ?, PantherCollection<T>> toImmutableSet() {
		return Collector.of(PantherSet::new, PantherCollection::add,
				(left, right) -> {
					left.addAll(right);
					return ImmutablePantherCollection.of(left);
				});
	}

	/**
	 * Process mapping functions to convert a stream query into a valid labyrinth map.
	 *
	 * @param keyMapper The key mapper.
	 * @param valueMapper The value mapper.
	 * @param <T> The key type
	 * @param <K> The value type
	 * @param <U> The new value type
	 * @return a fresh labyrinth map collector.
	 */
	public static <T, K, U> Collector<T, ?, PantherMap<K, U>> toMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper) {
		return Collector.of(PantherEntryMap::new,
				defaultJavaEntryAccumulation(keyMapper, valueMapper),
				defaultJavaEntryMerger());
	}

	/**
	 * Process mapping functions to convert a stream query into a valid immutable labyrinth map.
	 *
	 * @param keyMapper The key mapper.
	 * @param valueMapper The value mapper.
	 * @param <T> The key type
	 * @param <K> The value type
	 * @param <U> The new value type
	 * @return a fresh immutable labyrinth map collector.
	 */
	public static <T, K, U> Collector<T, ?, PantherMap<K, U>> toImmutableMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper) {
		return Collector.of(PantherEntryMap::new,
				defaultJavaEntryAccumulation(keyMapper, valueMapper),
				immutableJavaEntryMerger());
	}

	private static <T, K, V> BiConsumer<PantherMap<K, V>, T> defaultJavaEntryAccumulation(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
		return (map, element) -> {
			K k = keyMapper.apply(element);
			V v = Objects.requireNonNull(valueMapper.apply(element), "Cannot copy null map entry value for key " + k);
			map.computeIfAbsent(k, v);
		};
	}

	private static <K, V, M extends PantherMap<K, V>> BinaryOperator<M> defaultJavaEntryMerger() {
		return (m1, m2) -> {
			for (Map.Entry<K, V> e : m2.entries()) {
				K k = e.getKey();
				V v = Objects.requireNonNull(e.getValue(), "Cannot merge null map entry value for key " + k);
				m1.computeIfAbsent(k, v);
			}
			return m1;
		};
	}

	private static <K, V> BinaryOperator<PantherMap<K, V>> immutableJavaEntryMerger() {
		return (m1, m2) -> {
			for (Map.Entry<K, V> e : m2.entries()) {
				K k = e.getKey();
				V v = Objects.requireNonNull(e.getValue(), "Cannot merge null immutable map entry value for key " + k);
				m1.computeIfAbsent(k, v);
			}
			return ImmutablePantherMap.of(m1);
		};
	}

}
