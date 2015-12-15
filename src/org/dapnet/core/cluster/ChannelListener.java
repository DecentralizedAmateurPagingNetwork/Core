/*
 * DAPNET CORE PROJECT
 * Copyright (C) 2015
 *
 * Daniel Sialkowski
 *
 * daniel.sialkowski@rwth-aachen.de
 *
 * Institut für Hochfrequenztechnik
 * RWTH AACHEN UNIVERSITY
 * Melatener Str. 25
 * 52074 Aachen
 */

package org.dapnet.core.cluster;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.DAPNetCore;
import org.dapnet.core.HashUtil;
import org.dapnet.core.model.Node;
import org.dapnet.core.model.User;
import org.jgroups.Channel;
import org.jgroups.Event;
import org.jgroups.stack.IpAddress;

import java.util.Scanner;

public class ChannelListener implements org.jgroups.ChannelListener {
    private static final Logger logger = LogManager.getLogger(ChannelListener.class.getName());
    private ClusterManager clusterManager;

    public ChannelListener(ClusterManager clusterManager) {
        this.clusterManager = clusterManager;
    }

    public void channelConnected(Channel channel) {
        //Get State
        try {
            channel.getState(null, 0);
        } catch (Exception e) {
            logger.fatal("Could not get State");
            logger.catching(e);
            DAPNetCore.stopDAPNetCore();
        }

        //Creating Cluster?
        if (clusterManager.getChannel().getView().size() == 1) {
            printCreateClusterWarning();

            //Node already existing in State?
            if (clusterManager.getState().getNodes().contains(channel.getName())) {
                updateFirstNode();
            } else {
                createFirstNode();
            }

            //User already existing in State?
            if (clusterManager.getState().getUsers().size() == 0) {
                createFirstUser();
            }
        } else {
            //Update NodeStatus in existing Cluster to online
            if (!clusterManager.updateNodeStatus(Node.Status.ONLINE)) {
                logger.error("Could not update NodeStatus");
            }
        }
    }

    public void channelDisconnected(Channel channel) {
        //Nothing to do
    }

    public void channelClosed(Channel channel) {
        //Nothing to do
    }

    //Helper
    private void createFirstNode() {
        System.out.println("Creating first node:");
        System.out.println("Key (same as in ClusterConfig.xml):");
        Scanner scanner = new Scanner(System.in);
        String key = scanner.nextLine();

        System.out.println("Longitude:");
        String longitude = scanner.nextLine();

        System.out.println("Latitude:");
        String latitude = scanner.nextLine();

        IpAddress address = (IpAddress) clusterManager.getChannel().
                down(new Event(Event.GET_PHYSICAL_ADDRESS, clusterManager.getChannel().getAddress()));

        Node node = new Node(clusterManager.getChannel().getName(), address, longitude, latitude, Node.Status.ONLINE,
                key);
        try {
            node.setKey(HashUtil.createHash(node.getKey()));
        } catch (Exception e) {
            logger.catching(e);
            logger.fatal("First node could not been created");
            DAPNetCore.stopDAPNetCore();
        }

        if (clusterManager.handleStateOperation(null, "putNode", new Object[]{node}, new Class[]{Node.class})) {
            logger.info("First node successfully created");
        } else {
            logger.fatal("First node could not been created");
            DAPNetCore.stopDAPNetCore();
        }
    }

    private void updateFirstNode() {
        IpAddress address = (IpAddress) clusterManager.getChannel().
                down(new Event(Event.GET_PHYSICAL_ADDRESS, clusterManager.getChannel().getAddress()));

        Node node = clusterManager.getState().getNodes().findByName(clusterManager.getChannel().getName());
        node.setAddress(address);
        node.setStatus(Node.Status.ONLINE);

        if (clusterManager.handleStateOperation(null, "putNode", new Object[]{node}, new Class[]{Node.class})) {
            logger.info("First node successfully updated");
        } else {
            logger.fatal("First node could not been created");
            DAPNetCore.stopDAPNetCore();
        }
    }

    private void createFirstUser() {
        System.out.println("Creating first User (Admin):");
        System.out.println("Username:");
        Scanner scanner = new Scanner(System.in);
        String username = scanner.nextLine();
        System.out.println("Password:");
        String password = scanner.nextLine();
        System.out.println("Mail:");
        String mail = scanner.nextLine();

        User user = new User(username, password, mail, true);
        try {
            user.setHash(HashUtil.createHash(user.getHash()));
        } catch (Exception e) {
            logger.catching(e);
            logger.fatal("First user could not been created");
            DAPNetCore.stopDAPNetCore();
        }

        if (clusterManager.handleStateOperation(null, "putUser", new Object[]{user}, new Class[]{User.class})) {
            logger.info("First user successfully updated");
        } else {
            logger.fatal("First user could not been created");
            DAPNetCore.stopDAPNetCore();
        }
    }

    private void printCreateClusterWarning() {
        System.out.println("Creating new Cluster:");
        System.out.println("You are not joining an existing cluster.");
        System.out.println("If this is not your intention, please check your configuration and restart.");
    }
}
