package org.dapnet.core.model;

import java.util.Collection;
import java.util.Map;

/**
 * Repository interface providing access to the stored models.
 * 
 * @author Philipp Thiel
 */
public interface Repository {

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

}
