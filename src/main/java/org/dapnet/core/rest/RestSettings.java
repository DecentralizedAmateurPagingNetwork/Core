/*
 * DAPNET CORE PROJECT
 * Copyright (C) 2016
 *
 * Daniel Sialkowski
 *
 * daniel.sialkowski@rwth-aachen.de
 *
 * Institute of High Frequency Technology
 * RWTH AACHEN UNIVERSITY
 * Melatener Str. 25
 * 52074 Aachen
 */

package org.dapnet.core.rest;

import java.io.Serializable;

/**
 * This class contains the settings for the REST service.
 * 
 * @author Philipp Thiel
 */
public class RestSettings implements Serializable {
	private static final long serialVersionUID = 4524821306768847015L;
	private String hostname = "localhost";
	private int port = 8080;
	private String path = "/";
	private boolean logRequests = true;
	private boolean logResponses = true;
	private boolean jsonFilterIpAddresses = false;

	/**
	 * Returns the host name to listen on.
	 * 
	 * @return Host name (or IP Address) to listen on.
	 */
	public String getHostname() {
		return hostname;
	}

	/**
	 * Returns the port to listen on.
	 * 
	 * @return Port to listen on.
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Returns the path to listen on.
	 * 
	 * @return Path to listen on.
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Whether to log REST requests
	 * 
	 * @return True to log requests.
	 */
	public boolean logRequests() {
		return logRequests;
	}

	/**
	 * Whether to log REST responses.
	 * 
	 * @return True to log responses.
	 */
	public boolean logResponses() {
		return logResponses;
	}

	/**
	 * Whether to filter out IP addresses during JSON serialization.
	 * 
	 * @return True to filter out IP addresses.
	 */
	public boolean jsonFilterIpAddresses() {
		return jsonFilterIpAddresses;
	}

}
