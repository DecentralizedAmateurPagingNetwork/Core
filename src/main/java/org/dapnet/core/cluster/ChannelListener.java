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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.DAPNETCore;
import org.dapnet.core.HashUtil;
import org.dapnet.core.model.Node;
import org.dapnet.core.model.User;
import org.jgroups.Channel;
import org.jgroups.Event;
import org.jgroups.stack.IpAddress;

public class ChannelListener implements org.jgroups.ChannelListener {
	private static final Logger logger = LogManager.getLogger();
	private final ClusterManager clusterManager;

	public ChannelListener(ClusterManager clusterManager) {
		this.clusterManager = clusterManager;
	}

	@Override
	public void channelConnected(Channel channel) {
		try {
			channel.getState(null, 0);
		} catch (Exception e) {
			logger.fatal("Could not get state from cluster.", e);
			DAPNETCore.shutdown();
		}

		// Creating Cluster?
		if (clusterManager.getChannel().getView().size() == 1) {
			printCreateClusterWarning();

			// Node already existing in State?
			if (clusterManager.getState().getNodes().containsKey(channel.getName())) {
				updateFirstNode();
			} else {
				createFirstNode();
			}

			// User already existing in State?
			if (clusterManager.getState().getUsers().size() == 0) {
				createFirstUser();
			}
		} else {
			// Is performed automatically by each node!
			// Update NodeStatus in existing Cluster to online
			// if (!clusterManager.updateNodeStatus(Node.Status.ONLINE)) {
			// logger.error("Could not update NodeStatus");
			// }
		}
	}

	@Override
	public void channelDisconnected(Channel channel) {
	}

	@Override
	public void channelClosed(Channel channel) {
	}

	private void createFirstNode() {
		logger.info("Creating first node");

		// Get own IP address
		IpAddress address = (IpAddress) clusterManager.getChannel()
				.down(new Event(Event.GET_PHYSICAL_ADDRESS, clusterManager.getChannel().getAddress()));
		// Create new node
		Node node = new Node(clusterManager.getChannel().getName(), address, "0", "0", Node.Status.ONLINE);
		if (clusterManager.handleStateOperation(null, "putNode", new Object[] { node }, new Class[] { Node.class })) {
			logger.info("First node successfully created");
		} else {
			logger.fatal("First node could not been created");
			DAPNETCore.shutdown();
		}
	}

	private void updateFirstNode() {
		IpAddress address = (IpAddress) clusterManager.getChannel()
				.down(new Event(Event.GET_PHYSICAL_ADDRESS, clusterManager.getChannel().getAddress()));

		Node node = clusterManager.getState().getNodes().get(clusterManager.getChannel().getName());
		node.setAddress(address);
		node.setStatus(Node.Status.ONLINE);
		clusterManager.getState().writeToFile();
		logger.info("First node successfully updated");
	}

	private void createFirstUser() {
		logger.info("Creating first user");
		User user = new User("admin", "admin", "admin@admin.de", true);
		try {
			user.setHash(HashUtil.createHash(user.getHash()));
		} catch (Exception e) {
			logger.catching(e);
			logger.fatal("First user could not been created");
			DAPNETCore.shutdown();
		}

		if (clusterManager.handleStateOperation(null, "putUser", new Object[] { user }, new Class[] { User.class })) {
			logger.info("First user successfully updated");
		} else {
			logger.fatal("First user could not be created");
			DAPNETCore.shutdown();
		}
	}

	private void printCreateClusterWarning() {
		logger.warn("Creating new Cluster: Check configuration and restart in case you want to join an existing one");
	}
}
