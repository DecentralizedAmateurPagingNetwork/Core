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

import java.io.DataInput;
import java.io.DataOutput;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.HashUtil;
import org.dapnet.core.model.Node;
import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.annotations.Property;
import org.jgroups.auth.AuthToken;
import org.jgroups.util.Bits;
import org.jgroups.util.Util;

public class ClusterAuthentication extends AuthToken {
	private static final Logger logger = LogManager.getLogger();
	private static ClusterManager clusterManager;

	@Property(exposeAsManagedAttribute = false)
	private String auth_value = null;

	public ClusterAuthentication() {
	}

	public ClusterAuthentication(String authvalue) {
		this.auth_value = authvalue;
	}

	@Override
	public String getName() {
		return "org.dpnet.core.cluster.ClusterAuthentication";
	}

	@Override
	public boolean authenticate(AuthToken token, Message msg) {
		if (clusterManager == null) {
			logger.error("Authentication has no reference to ClusterManager");
			return false;
		}

		Address sender = msg.getSrc();
		Node node = clusterManager.getState().getNodes().get(sender.toString());
		if (node == null) {
			logger.warn("Authentication of Node {} failed: Unknown node", sender);
			return false;
		}

		try {
			if (!HashUtil.validatePassword(((ClusterAuthentication) token).getAuthValue(), node.getKey())) {
				logger.warn("Authentication of Node {} failed: Wrong key", sender);
				return false;
			}
		} catch (Exception e) {
			logger.catching(e);
			logger.warn("Authentication of Node {} failed", sender);
			return false;
		}

		return true;
	}

	@Override
	public void writeTo(DataOutput out) throws Exception {
		Bits.writeString(auth_value, out);
	}

	@Override
	public void readFrom(DataInput in) throws Exception {
		auth_value = Bits.readString(in);
	}

	@Override
	public int size() {
		return Util.size(auth_value);
	}

	public static void setClusterManger(ClusterManager clusterMangerParam) {
		clusterManager = clusterMangerParam;
	}

	public String getAuthValue() {
		return auth_value;
	}
}
