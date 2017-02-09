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
import org.dapnet.core.model.Node;
import org.jgroups.*;
import org.jgroups.stack.IpAddress;

public class MembershipListener implements org.jgroups.MembershipListener {
	private static final Logger logger = LogManager.getLogger(MembershipListener.class.getName());
	private ClusterManager clusterManager;

	public MembershipListener(ClusterManager clusterManager) {
		this.clusterManager = clusterManager;
	}

	@Override
	public void viewAccepted(View view) {
		new ViewHandler(view).start();
	}

	@Override
	public void suspect(Address address) {
		logger.warn("Node " + address + " is suspected");
	}

	@Override
	public void block() {
		// momentarily not used
		// called in case of FLUSH Block
	}

	@Override
	public void unblock() {
		// momentarily not used
	}

	private class ViewHandler extends Thread {
		View view;

		private ViewHandler(View view) {
			this.view = view;
		}

		public void run() {
			logger.info("New View: " + view);
			// Check whether merge is taking place
			if (view instanceof MergeView) {
				try {
					handleMerge((MergeView) view);
				} catch (Exception e) {
					logger.fatal("Could not get State from majority");
					logger.fatal(e);
					DAPNETCore.stopDAPNETCore();
					return;
				}
			}

			for (Address add : view.getMembers()) {
				PhysicalAddress physicalAddress = (PhysicalAddress) clusterManager.getChannel()
						.down(new Event(Event.GET_PHYSICAL_ADDRESS, add));
				if (physicalAddress instanceof IpAddress) {
					Node n = clusterManager.getState().getNodes().get(add.toString());
					if (n != null) {
						n.setAddress((IpAddress) physicalAddress);
					}
				}
			}

			// Update node states:
			updateNodeStates();

			// Save and check for quorum:
			clusterManager.checkQuorum();
			clusterManager.getState().writeToFile();
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
				 * fatal("Node has quorum although it is the minoritySubgroup "
				 * + "(Seems to merge independent clusters). " +
				 * "Stopping minority group."); DAPNETCore.stopDAPNETCore(); }
				 * else {
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
						if (numberOfAttempts++ > 5)
							throw e;
					}
				}

				// Reconnect to transmitters (transmitters might have been
				// edited in major subgroup)
				// clusterManager.getTransmitterManager().performReconnect(clusterManager.getNodeTransmitter());
				// }
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
			for (Node node : clusterManager.getState().getNodes().values()) {
				if (view.getMembers().stream().filter(m -> m.toString().equals(node.getName())).findFirst()
						.isPresent()) {
					if (node.getStatus() == Node.Status.SUSPENDED) {
						node.setStatus(Node.Status.ONLINE);
						logger.info("Changed status of " + node.getName() + " from " + Node.Status.SUSPENDED + " to "
								+ Node.Status.ONLINE);
					} else if (node.getStatus() == Node.Status.UNKNOWN) {
						node.setStatus(Node.Status.ONLINE);
						logger.info("Changed status of " + node.getName() + " from " + Node.Status.UNKNOWN + " to "
								+ Node.Status.ONLINE);
					}
					// else if node in ONLINE which is the correct status
				} else {
					// Known node is not present in view
					if (node.getStatus() == Node.Status.ONLINE) {
						node.setStatus(Node.Status.UNKNOWN);
						logger.warn("Changed status of " + node.getName() + " from " + Node.Status.ONLINE + " to "
								+ Node.Status.UNKNOWN);
					}
					// else if node is UNKNOWN or SUSPENDED which is the correct
					// status
				}
			}
		}
	}
}
