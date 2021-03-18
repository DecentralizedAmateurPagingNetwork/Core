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

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.DAPNETCore;
import org.dapnet.core.HashUtil;
import org.dapnet.core.Settings;
import org.dapnet.core.model.NamedObject;
import org.dapnet.core.model.Node;
import org.dapnet.core.model.StateManager;
import org.dapnet.core.model.User;
import org.jgroups.Channel;
import org.jgroups.Event;
import org.jgroups.stack.IpAddress;

public class ChannelListener implements org.jgroups.ChannelListener {
	private static final Logger logger = LogManager.getLogger();
	private final ClusterManager clusterManager;

	public ChannelListener(ClusterManager clusterManager) {
		this.clusterManager = Objects.requireNonNull(clusterManager, "Cluster manager must not be null.");
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

			// XXX I'm not sure if holding the lock while the RPC call is executed might
			// lead to a dead-lock. That's why this is looking a bit awkward...
			boolean createUser = false;
			boolean createNode = false;

			final StateManager sm = clusterManager.getStateManager();
			Lock lock = sm.getLock().writeLock();
			lock.lock();

			try {
				// User already existing in State?
				createUser = sm.getRepository().getUsers().isEmpty();

				// Node already existing in State?
				if (!sm.getRepository().getNodes().containsKey(NamedObject.normalizeName(channel.getName()))) {
					createNode = true;
				}
			} finally {
				lock.unlock();
			}

			if (createUser) {
				createFirstUser();
			}

			if (createNode) {
				createFirstNode();
			} else {
				updateFirstNode(sm);
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
		node.setOwnerNames(Set.of("admin"));
		if (clusterManager.handleStateOperation(null, "putNode", new Object[] { node }, new Class[] { Node.class })) {
			logger.info("First node successfully created");
		} else {
			logger.fatal("First node could not been created");
			DAPNETCore.shutdown();
		}
	}

	private void updateFirstNode(StateManager stateManager) {
		IpAddress address = (IpAddress) clusterManager.getChannel()
				.down(new Event(Event.GET_PHYSICAL_ADDRESS, clusterManager.getChannel().getAddress()));

		Lock lock = stateManager.getLock().writeLock();
		lock.lock();

		try {
			Node node = stateManager.getRepository().getNodes()
					.get(NamedObject.normalizeName(clusterManager.getChannel().getName()));
			node.setAddress(address);
			node.setStatus(Node.Status.ONLINE);
		} finally {
			lock.unlock();
		}

		try {
			stateManager.writeStateToFile(Settings.getModelSettings().getStateFile());
		} catch (Exception ex) {
			logger.fatal("Failed to write state file.", ex);
		}

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
