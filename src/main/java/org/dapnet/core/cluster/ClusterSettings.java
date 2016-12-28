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

package org.dapnet.core.cluster;

import java.io.Serializable;

public class ClusterSettings implements Serializable {
	private static final long serialVersionUID = 6362766257617737103L;
	private int responseTimeout = 10000;
	private String clusterConfigurationFile = "config/ClusterConfig.xml";

	public int getResponseTimeout() {
		return responseTimeout;
	}

	public String getClusterConfigurationFile() {
		return clusterConfigurationFile;
	}
}
