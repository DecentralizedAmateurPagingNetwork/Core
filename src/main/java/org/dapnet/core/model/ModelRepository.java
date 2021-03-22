package org.dapnet.core.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * An interface for a model repository. It is basically a stripped-down version
 * of a map but the implementation can be replaced. The key is case-insensitive.
 * 
 * @author Philipp Thiel
 *
 * @param <T> Object type
 */
public interface ModelRepository<T> {

	/**
	 * Gets an unmodifyable collection of all values. Note that this is a one-time
	 * snapshot of the state and getting the collection might take some time,
	 * depending on the implementation. It is advisable to keep a reference to the
	 * collection for immediate operations.
	 * 
	 * @return Value collection
	 */
	Collection<T> values();

	/**
	 * Gets a value by the given key.
	 * 
	 * @param key Key
	 * @return Stored value or {@code null} if not found
	 */
	T get(String key);

	/**
	 * Puts the given value into the repository, returning the previous value.
	 * 
	 * @param key   Key
	 * @param value Value to put
	 * @return Previous value that can be {@code null} if there wasn't any
	 */
	T put(String key, T value);

	/**
	 * Removes a value by the given key.
	 * 
	 * @param key Key
	 * @return Previous value or {@code null} if not found
	 */
	T remove(String key);

	/**
	 * Checks if the given key is known.
	 * 
	 * @param key Key
	 * @return {@code true} if the key exists, {@code false} otherwise
	 */
	boolean containsKey(String key);

	/**
	 * Checks if the repository is empty.
	 * 
	 * @return {@code true} if the repository is empty
	 */
	boolean isEmpty();

	/**
	 * Gets the size of the repository (i.e. number of stored values).
	 * 
	 * @return Repository size
	 */
	int size();

	/**
	 * Gets a set of values for the given key set. If a key is not found there will
	 * be no error.
	 * 
	 * @param keys Set of keys to look up
	 * @return Set of values
	 */
	default Set<T> get(Set<String> keys) {
		Set<T> result = new HashSet<>();
		for (String key : keys) {
			T obj = get(key);
			if (obj != null) {
				result.add(obj);
			}
		}

		return result;
	}

	/**
	 * Gets a set of values for the given key set.
	 * 
	 * @param keys           Set of keys to look up
	 * @param throwException {@code true} if a missing key should raise an exception
	 * @return Set of values
	 * @throws NoSuchElementException if a key is not found and
	 *                                {@code throwException == true}
	 */
	default Set<T> get(Set<String> keys, boolean throwException) {
		Set<T> result = new HashSet<>();
		for (String key : keys) {
			T obj = get(key);
			if (obj != null) {
				result.add(obj);
			} else if (throwException) {
				throw new NoSuchElementException(key);
			}
		}

		return result;
	}

}
