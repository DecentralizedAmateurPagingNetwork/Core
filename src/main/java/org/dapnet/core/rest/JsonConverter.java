package org.dapnet.core.rest;

import org.dapnet.core.rest.RestSecurity.SecurityStatus;

/**
 * Interface for serializing/deserializing JSON.
 * 
 * @author Philipp Thiel
 */
public interface JsonConverter {

	/**
	 * Deserializes a JSON string into an object of the specified class.
	 * 
	 * @param <T>  Object type
	 * @param json JSON to parse
	 * @param type Target class
	 * @return Deserialized object or {@code null} if input is {@code null} or empty
	 */
	<T> T fromJson(String json, Class<T> type);

	/**
	 * Serializes an object into a JSON string.
	 * 
	 * @param source Object to serialize
	 * @return JSON string or {@code null} if {@code source == null}
	 */
	String toJson(Object source);

	/**
	 * Serializes an object into a JSON string and masks fields based on the
	 * security status.
	 * 
	 * @param source Object to serialize
	 * @param status Security status
	 * @return JSON string or {@code null} if {@code source == null}
	 */
	String toJson(Object source, SecurityStatus status);

}
