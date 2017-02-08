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

	public String getHostname() {
		return hostname;
	}

	public int getPort() {
		return port;
	}

}
