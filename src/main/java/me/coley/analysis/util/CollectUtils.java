package me.coley.analysis.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Collection utilities.
 *
 * @author Matt Coley
 */
public class CollectUtils {
	/**
	 * @param src
	 * 		Original list.
	 * @param additional
	 * 		Item to add.
	 * @param <T>
	 * 		Type of content.
	 *
	 * @return New list with additional item.
	 */
	public static <T> List<T> add(List<T> src, T additional) {
		List<T> list = new ArrayList<>(src);
		list.add(additional);
		return list;
	}

	/**
	 * @param src1
	 * 		Original list.
	 * @param src2
	 * 		Additional items to add.
	 * @param <T>
	 * 		Type of content.
	 *
	 * @return New list with additional items.
	 */
	public static <T> List<T> combine(List<T> src1, List<T> src2) {
		List<T> list = new ArrayList<>(src1);
		list.addAll(src2);
		return list;
	}

	/**
	 * @param src1
	 * 		Original list.
	 * @param src2
	 * 		Additional list.
	 * @param additional
	 * 		Additional item to add.
	 * @param <T>
	 * 		Type of content.
	 *
	 * @return New list with additional items.
	 */
	public static <T> List<T> combineAdd(List<T> src1, List<T> src2, T additional) {
		return add(combine(src1, src2), additional);
	}

	/**
	 * @param src
	 * 		Original list.
	 * @param <T>
	 * 		Type of content.
	 *
	 * @return List with duplicates removed.
	 */
	public static <T> List<T> distinct(List<T> src) {
		List<T> copy = new ArrayList<>();
		for(T t : src) {
			if (copy.contains(t))
				continue;
			copy.add(t);
		}
		return copy;
	}
}
