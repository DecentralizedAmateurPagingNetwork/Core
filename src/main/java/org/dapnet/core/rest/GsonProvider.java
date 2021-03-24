package org.dapnet.core.rest;

import org.dapnet.core.rest.RestSecurity.SecurityStatus;

import com.google.gson.Gson;

/**
 * Interface for obtaining gson instances for different purposes.
 * 
 * @author Philipp Thiel
 */
public interface GsonProvider {

	/**
	 * Gets the gson instance used for processing requests.
	 * 
	 * @return Gson instance
	 */
	Gson getForRequest();

	/**
	 * Gets the gson instance used for processing general responses. This is usually
	 * not filtered.
	 * 
	 * @return Gson instance
	 */
	Gson getForResponse();

	/**
	 * Gets the gson instance used for processing security-filtered responses.
	 * 
	 * @param status Security status (i.e. access level)
	 * @return Gson instance
	 */
	Gson getForResponse(SecurityStatus status);

}
