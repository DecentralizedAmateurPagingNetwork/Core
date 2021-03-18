package org.dapnet.core.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * Repository interface providing access to the stored models.
 * 
 * @author Philipp Thiel
 */
public interface Repository {

	/**
	 * Gets the read-write lock that can be used to synchronize repository access.
	 * 
	 * @return Lock instance
	 */
	ReadWriteLock getLock();

	/**
	 * Gets the collection of all calls.
	 * 
	 * @return Call collection
	 */
	Collection<Call> getCalls();

	/**
	 * Gets the callsign map.
	 * 
	 * @return Callsign map
	 */
	Map<String, CallSign> getCallSigns();

	/**
	 * Gets the node map.
	 * 
	 * @return Node map
	 */
	Map<String, Node> getNodes();

	/**
	 * Gets the user map.
	 * 
	 * @return User map
	 */
	Map<String, User> getUsers();

	/**
	 * Gets the transmitter map.
	 * 
	 * @return Transmitter map
	 */
	Map<String, Transmitter> getTransmitters();

	/**
	 * Gets the transmitter group map.
	 * 
	 * @return Transmitter group map
	 */
	Map<String, TransmitterGroup> getTransmitterGroups();

	/**
	 * Gets the rubric map.
	 * 
	 * @return Rubric map
	 */
	Map<String, Rubric> getRubrics();

	/**
	 * Gets the news map.
	 * 
	 * @return News map
	 */
	Map<String, NewsList> getNews();

	/**
	 * Gets the statistics.
	 * 
	 * @return Statistics
	 */
	CoreStatistics getStatistics();

	public static <T> T getObject(Map<String, T> map, String name) {
		return map.get(NamedObject.normalizeName(name));
	}

	public static <T> Collection<T> getObjects(Map<String, T> map, Set<String> names) {
		return getObjects(map, names, false);
	}

	public static <T> Collection<T> getObjects(Map<String, T> map, Set<String> names, boolean throwException) {
		if (names == null) {
			return null;
		}

		Collection<T> result = new LinkedList<>();
		for (String name : names) {
			T obj = map.get(NamedObject.normalizeName(name));
			if (obj != null) {
				result.add(obj);
			} else if (throwException) {
				throw new NoSuchElementException(name);
			}
		}

		return result;
	}

}
