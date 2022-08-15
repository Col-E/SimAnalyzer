package me.coley.analysis.util;

/**
 * Wrapped value.
 *
 * @param <T>
 * 		Value type.
 *
 * @author Matt Coley
 */
public class GetSet<T> {
	private T value;

	/**
	 * Initialize wrapper with value.
	 *
	 * @param value
	 * 		Initial value.
	 */
	public GetSet(T value) {
		this.value = value;
	}

	/**
	 * @return Wrapped value.
	 */
	public T get() {
		return value;
	}

	/**
	 * @param value
	 * 		New wrapped value.
	 */
	public void set(T value) {
		this.value = value;
	}
}
