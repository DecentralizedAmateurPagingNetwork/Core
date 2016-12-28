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
import org.dapnet.core.HashUtil;
import org.dapnet.core.model.Node;
import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.annotations.Property;
import org.jgroups.auth.AuthToken;
import org.jgroups.util.Bits;
import org.jgroups.util.Util;

import java.io.DataInput;
import java.io.DataOutput;

public class ClusterAuthentication extends AuthToken {
    private static final Logger logger = LogManager.getLogger(ClusterAuthentication.class.getName());
    private static ClusterManager clusterManager;

    @Property(exposeAsManagedAttribute = false)
    private String auth_value = null;

    public ClusterAuthentication() {
    }

    public ClusterAuthentication(String authvalue) {
        this.auth_value = authvalue;
    }

    public String getName() {
        return "org.dpnet.core.cluster.ClusterAuthentication";
    }

    public boolean authenticate(AuthToken token, Message msg) {
        Address sender = msg.getSrc();
        /*IpAddress address =
                sender != null ? (IpAddress) auth.down(new Event(Event.GET_PHYSICAL_ADDRESS, sender)) : null;*/
        if (clusterManager == null) {
            logger.error("Authentication has no reference to ClusterManager");
            return false;
        }

        Node node = clusterManager.getState().getNodes().findByName(sender.toString());
        if (node == null) {
            logger.warn("Authentication of Node " + sender.toString() + " failed: Unknown node");
            return false;
        }
        /*if (!address.equals(node.getAddress())) {
            logger.warn("Authentication of Node " + sender.toString() + " failed: Received ip address "
                    + address.getIpAddress() + " is unequal to " + node.getAddress());
            return false;
        }*/

        try {
            if (!HashUtil.validatePassword(((ClusterAuthentication) token).getAuthValue(), node.getKey())) {
                logger.warn("Authentication of Node " + sender.toString() + " failed: Wrong key");
                return false;
            }
        } catch (Exception e) {
            logger.catching(e);
            logger.warn("Authentication of Node " + sender.toString() + " failed");
            return false;
        }
        return true;
    }

    public void writeTo(DataOutput out) throws Exception {
        Bits.writeString(this.auth_value, out);
    }

    public void readFrom(DataInput in) throws Exception {
        this.auth_value = Bits.readString(in);
    }

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
