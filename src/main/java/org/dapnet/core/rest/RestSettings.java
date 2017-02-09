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

public class RestSettings implements Serializable {
	private static final long serialVersionUID = 4524821306768847015L;
	private String hostname = "localhost";
	private int port = 8080;
	private String path = "/";

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

}
