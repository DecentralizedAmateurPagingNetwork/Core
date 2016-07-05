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

package org.dapnet.core.transmission;

import java.io.Serializable;

public class TransmissionSettings implements Serializable {
    private PagingProtocolSettings pagingProtocolSettings = new PagingProtocolSettings();
    private Raspager1Settings raspager1Settings = new Raspager1Settings();

    public PagingProtocolSettings getPagingProtocolSettings() {
        return pagingProtocolSettings;
    }

    public Raspager1Settings getRaspager1Settings() {
        return raspager1Settings;
    }

    public class PagingProtocolSettings implements Serializable{
        private int numberOfSyncLoops = 5;
        private int sendSpeed = 1;// 0: 512, 1: 1200, 2:2400
        private String activationCode = "0 7 50,0 7 34,0 7 53,0 7 51,0 7 51,0 7 52,0 7 52,0 7 52";

        public int getNumberOfSyncLoops() {
            return numberOfSyncLoops;
        }

        public int getSendSpeed() {
            return sendSpeed;
        }

        public String getActivationCode() {
            return activationCode;
        }
    }

    public class Raspager1Settings implements Serializable{
        private int maxNumberOfReconnects = -1;
        private int reconnectWaitTime = 20 * 1000;
        private int connectionTimeout = 20000;
        private int maxMessageCount = 4;
        private int transmissionDelay = 500;

        public int getConnectionTimeout() {
            return connectionTimeout;
        }

        public int getReconnectWaitTime() {
            return reconnectWaitTime;
        }

        public int getMaxNumberOfReconnects() {
            return maxNumberOfReconnects;
        }

        public int getMaxMessageCount() {
            return maxMessageCount;
        }

        public int getTransmissionDelay() {
            return transmissionDelay;
        }
    }
}
