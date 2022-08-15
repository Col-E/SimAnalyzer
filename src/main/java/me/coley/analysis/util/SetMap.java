package me.coley.analysis.util;

import java.util.*;

/**
 * Minimal multi-map implementation.
 *
 * @param <K>
 * 		Key type.
 * @param <V>
 * 		Value type.
 *
 * @author Matt Coley
 */
public class SetMap<K, V> extends HashMap<K, Set<V>> {
	/**
	 * Add single key-value pair.
	 *
	 * @param key
	 * 		Key.
	 * @param value
	 * 		Value to add to value-set.
	 */
	public void putSingle(K key, V value) {
		computeIfAbsent(key, k -> new HashSet<>()).add(value);
	}

	/**
	 * Check for key-value pair.
	 *
	 * @param key
	 * 		Key.
	 * @param value
	 * 		Value in value set related to key.
	 *
	 * @return {@code true} when value exists in the value set related to the key.
	 */
	public boolean contains(K key, V value) {
		Set<V> set = get(key);
		if (set == null)
			return false;
		return set.contains(value);
	}
}