package org.dapnet.core.model;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;

import jakarta.validation.ConstraintViolation;

/**
 * DAPNET Core repository interface providing access to the models.
 * 
 * @author Philipp Thiel
 */
public interface CoreRepository {

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
	ModelRepository<CallSign> getCallSigns();

	/**
	 * Gets the node map.
	 * 
	 * @return Node map
	 */
	ModelRepository<Node> getNodes();

	/**
	 * Gets the user map.
	 * 
	 * @return User map
	 */
	ModelRepository<User> getUsers();

	/**
	 * Gets the transmitter map.
	 * 
	 * @return Transmitter map
	 */
	ModelRepository<Transmitter> getTransmitters();

	/**
	 * Gets the transmitter group map.
	 * 
	 * @return Transmitter group map
	 */
	ModelRepository<TransmitterGroup> getTransmitterGroups();

	/**
	 * Gets the rubric map.
	 * 
	 * @return Rubric map
	 */
	ModelRepository<Rubric> getRubrics();

	/**
	 * Gets the news map.
	 * 
	 * @return News map
	 */
	ModelRepository<NewsList> getNews();

	/**
	 * Gets the statistics.
	 * 
	 * @return Statistics
	 */
	CoreStatistics getStatistics();

	/**
	 * Validates the given object.
	 * 
	 * @param <T>    Object type
	 * @param object Object to validate
	 * @return Set of constraint violations
	 */
	<T> Set<ConstraintViolation<T>> validate(T object);

}
