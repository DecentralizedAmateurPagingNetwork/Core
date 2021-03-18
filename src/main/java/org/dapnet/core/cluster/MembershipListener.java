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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.locks.Lock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.DAPNETCore;
import org.dapnet.core.Settings;
import org.dapnet.core.model.Node;
import org.dapnet.core.model.StateManager;
import org.jgroups.Address;
import org.jgroups.Event;
import org.jgroups.MergeView;
import org.jgroups.PhysicalAddress;
import org.jgroups.View;
import org.jgroups.stack.IpAddress;
import org.jgroups.util.ExtendedUUID;

public class MembershipListener implements org.jgroups.MembershipListener {
	private static final Logger logger = LogManager.getLogger();
	private final ClusterManager clusterManager;
	private final StateManager stateManager;

	public MembershipListener(ClusterManager clusterManager) {
		this.clusterManager = Objects.requireNonNull(clusterManager, "Cluster manager must not be null.");
		this.stateManager = Objects.requireNonNull(clusterManager.getStateManager(), "State manager must not be null.");
	}

	@Override
	public void viewAccepted(View view) {
		ViewHandler handler = new ViewHandler(view);
		Thread worker = new Thread(handler, "ViewHandler");
		worker.start();
	}

	@Override
	public void suspect(Address address) {
		logger.warn("Node {} is suspected", address);
	}

	@Override
	public void block() {
	}

	@Override
	public void unblock() {
	}

	private final class ViewHandler implements Runnable {
		private final View view;

		private ViewHandler(View view) {
			this.view = view;
		}

		@Override
		public void run() {
			logger.info("New View: {}", view);
			// Check whether merge is taking place
			if (view instanceof MergeView) {
				try {
					handleMerge((MergeView) view);
				} catch (Exception e) {
					logger.fatal("Could not get State from majority", e);
					DAPNETCore.shutdown();
					return;
				}
			}

			Lock lock = stateManager.getLock().writeLock();
			lock.lock();

			try {
				for (Address addr : view.getMembers()) {
					Node node = stateManager.getNodes().get(addr.toString());
					if (node == null) {
						logger.warn("Unknown node in view: " + addr);
						continue;
					}

					// Try to set version information
					if (addr instanceof ExtendedUUID) {
						ExtendedUUID extaddr = (ExtendedUUID) addr;
						byte[] buff = extaddr.get("version");
						if (buff != null) {
							node.setVersion(new String(buff, StandardCharsets.UTF_8));
						}
					}

					// Try to set IP address for node
					PhysicalAddress physicalAddress = (PhysicalAddress) clusterManager.getChannel()
							.down(new Event(Event.GET_PHYSICAL_ADDRESS, addr));
					if (physicalAddress instanceof IpAddress) {
						node.setAddress((IpAddress) physicalAddress);
					}
				}

				// Update node states:
				updateNodeStates();
			} finally {
				lock.unlock();
			}

			// Save and check for quorum:
			clusterManager.checkQuorum();

			try {
				stateManager.writeStateToFile(Settings.getModelSettings().getStateFile());
			} catch (IOException ex) {
				// Forward the exception
				throw new RuntimeException(ex);
			}
		}

		private void handleMerge(MergeView view) throws Exception {
			logger.info("Starting merge process");

			// Find major Subgroup
			View majorSubgroup = getMajorSubgroup(view);

			// In major Subgroup?
			if (majorSubgroup.containsMember(clusterManager.getChannel().getAddress())) {
				logger.info("Node is in majorSubgroup: Nothing to be done");
			} else {
				/*
				 * if (clusterManager.isQuorum()) { logger.
				 * fatal("Node has quorum although it is the minoritySubgroup " +
				 * "(Seems to merge independent clusters). " + "Stopping minority group.");
				 * DAPNETCore.stopDAPNETCore(); } else {
				 */
				logger.info("Node is minoritySubgroup");

				// Get State from majority
				logger.info("Receive State from majoritySubgroup");
				// Get State sometimes fails, no idea why!
				int numberOfAttempts = 0;
				while (true) {
					try {
						clusterManager.getChannel().getState(majorSubgroup.getMembers().get(0), 5000);
						break; // Success
					} catch (Exception e) {
						logger.warn("Failed to receive State");
						logger.warn(e);
						if (numberOfAttempts++ > 5) {
							throw e;
						}
					}
				}
			}

			logger.info("Finished merge process");
		}

		private View getMajorSubgroup(MergeView view) {
			// Major subgroups is the group with greatest number of nodes
			// If two groups have the same number of nodes, the first group is
			// taken as major subgroup
			View majorSubgroup = view.getSubgroups().get(0);
			for (View subgroup : view.getSubgroups()) {
				if (majorSubgroup.getMembers().size() < subgroup.getMembers().size()) {
					majorSubgroup = subgroup;
				}
			}

			return majorSubgroup;
		}

		private void updateNodeStates() {
			// All nodes in the view are already in state, since they would be
			// otherwise rejected while authorization
			// (expect of first node, which might not be in the state, but will
			// add itself immediately)
			for (Node node : stateManager.getNodes().values()) {
				Node.Status oldStatus = node.getStatus();
				if (view.getMembers().stream().filter(m -> m.toString().equalsIgnoreCase(node.getName())).findFirst()
						.isPresent()) {
					if (oldStatus == Node.Status.SUSPENDED || oldStatus == Node.Status.UNKNOWN) {
						node.setStatus(Node.Status.ONLINE);
						logger.info("Changed status of {} from {} to {}", node.getName(), oldStatus, node.getStatus());
					}
					// else if node in ONLINE which is the correct status
				} else {
					// Known node is not present in view
					if (oldStatus == Node.Status.ONLINE) {
						node.setStatus(Node.Status.UNKNOWN);
						logger.warn("Changed status of {} from {} to {}", node.getName(), oldStatus, node.getStatus());
					}
					// else if node is UNKNOWN or SUSPENDED which is the correct
					// status
				}
			}
		}
	}
}
