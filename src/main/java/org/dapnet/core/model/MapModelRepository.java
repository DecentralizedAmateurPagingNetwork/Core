package org.dapnet.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * A model repository implementation that is backed by a map.
 * 
 * @author Philipp Thiel
 *
 * @param <T> Object type
 */
final class MapModelRepository<T> implements ModelRepository<T> {

	private final Map<String, T> map;

	/**
	 * Constructs a new object instance.
	 * 
	 * @param map Map to use
	 * @throws NullPointerException if {@code map == null}
	 */
	public MapModelRepository(Map<String, T> map) {
		this.map = Objects.requireNonNull(map, "Map must not be null.");
	}

	@Override
	public Collection<T> values() {
		Collection<T> result = new ArrayList<>(map.values());
		return Collections.unmodifiableCollection(result);
	}

	@Override
	public T get(String key) {
		return map.get(NamedObject.normalize(key));
	}

	@Override
	public T put(String key, T value) {
		return map.put(NamedObject.normalize(key), value);
	}

	@Override
	public T remove(String key) {
		return map.remove(NamedObject.normalize(key));
	}

	@Override
	public boolean containsKey(String key) {
		return map.containsKey(NamedObject.normalize(key));
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public int size() {
		return map.size();
	}

}
