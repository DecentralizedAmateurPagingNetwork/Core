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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.DAPNETCore;
import org.dapnet.core.model.Node;
import org.dapnet.core.model.Transmitter;
import org.jgroups.Address;
import org.jgroups.MergeView;
import org.jgroups.View;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MembershipListener implements org.jgroups.MembershipListener {
    private static final Logger logger = LogManager.getLogger(MembershipListener.class.getName());
    private ClusterManager clusterManager;

    public MembershipListener(ClusterManager clusterManager) {
        this.clusterManager = clusterManager;
    }

    @Override
    public void viewAccepted(View view) {
        logger.info("New View: " + view);

        //Verify Nodes in View
        verifyNodes(view);

        //Merge?
        if (view instanceof MergeView) {
            handleMerge((MergeView) view);
        }
    }

    @Override
    public void suspect(Address address) {
        logger.warn("Node " + address + " is suspected");

        Node suspectedNode = clusterManager.getState().getNodes().findByName(address.toString());
        if (suspectedNode.getStatus() != Node.Status.UNKNOWN) {
            clusterManager.handleStateOperation(null, "updateNodeStatus",
                    new Object[]{address.toString(), Node.Status.UNKNOWN},
                    new Class[]{String.class, Node.Status.class});
        }
    }

    @Override
    public void block() {
        //momentarily not used
        //called in case of FLUSH Block
    }

    @Override
    public void unblock() {
        //momentarily not used
    }

    private void verifyNodes(View view) {
        //Look for Nodes with Status ONLINE, which are not in the view
        NodeLoop:
        for (Node node : clusterManager.getState().getNodes()) {
            if (node.getStatus() == Node.Status.ONLINE) {
                for (Address member : view.getMembers()) {
                    if (node.getName().equals(member.toString())) {
                        //Find Node in View
                        continue NodeLoop;
                    }
                }
                //Could not found Node in View
                logger.warn("Node " + node.getName() + " has Status ONLINE although it is not in the View");
                clusterManager.handleStateOperation(null, "updateNodeStatus",
                        new Object[]{node.getName(), Node.Status.UNKNOWN},
                        new Class[]{String.class, Node.Status.class});
            }
        }
    }

    private void handleMerge(MergeView view) {
        logger.info("Starting Merge Handler");

        //Find major Subgroup
        View majorSubgroup = getMajorSubgroup(view);

        //In major Subgroup?
        if (majorSubgroup.containsMember(clusterManager.getChannel().getAddress())) {
            logger.info("Node is in majorSubgroup: Nothing to be done");
        } else {
            if (clusterManager.isQuorum()) {
                logger.fatal("Node has quorum although it is the minoritySubgroup " +
                        "(Seems to merge independent clusters). " +
                        "Stopping minority group.");
                DAPNETCore.stopDAPNETCore();
            } else {
                logger.info("Node is minoritySubgroup. Receive State from majoritySubgroup");

                //Disconnect from all TransmitterDevices
                clusterManager.getTransmitterDeviceManager().disconnectFromAllTransmitters();

                //Get State from majority
                try {
                    clusterManager.getChannel().getState(majorSubgroup.getMembers().get(0), 0);
                } catch (Exception e) {
                    logger.fatal("Could not get State from majority");
                    logger.catching(e);
                    DAPNETCore.stopDAPNETCore();
                }

                //Inform Cluster that Node is ONLINE
                clusterManager.updateNodeStatus(Node.Status.ONLINE);

                //Reconnect to TransmitterDevices
                clusterManager.getTransmitterDeviceManager().connectToTransmitters(clusterManager.getNodeTransmitter());
            }
        }
    }

    private View getMajorSubgroup(MergeView view) {
        //If two groups have the same size, the first group is taken as major subgroup
        View majorSubgroup = view.getSubgroups().get(0);
        for (View subgroup : view.getSubgroups()) {
            if (majorSubgroup.getMembers().size() < subgroup.getMembers().size()) {
                majorSubgroup = subgroup;
            }
        }
        return majorSubgroup;
    }

    private List<Transmitter> getTransmittersCopy() {
        Gson gson = new Gson();
        String transmittersString = gson.toJson(clusterManager.getNodeTransmitter());
        Type listType = new TypeToken<ArrayList<Transmitter>>() {
        }.getType();
        return gson.fromJson(transmittersString, listType);
    }


}
