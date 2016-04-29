/*
 * DAPNET CORE PROJECT
 * Copyright (C) 2016
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

package org.dapnet.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.dapnet.core.cluster.ClusterManager;
import org.dapnet.core.rest.RestManager;
import org.dapnet.core.scheduler.SchedulerManager;
import org.dapnet.core.transmission.TransmissionManager;

import java.util.Locale;
import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;

public class DAPNETCore {
    private ClusterManager clusterManager;
    private RestManager restManager;
    private TransmissionManager transmissionManager;
    private SchedulerManager schedulerManager;

    private void start() {
        try {
            //Start
            System.out.println("Starting DAPNET_CORE...");
            logger.info("Starting TransmissionManager");
            transmissionManager = new TransmissionManager();
            logger.info("Starting Cluster");
            clusterManager = new ClusterManager(transmissionManager);
            logger.info("Starting RestManager");
            restManager = new RestManager(clusterManager);
            restManager.startServer();
            logger.info("Starting SchedulerManager");
            schedulerManager = new SchedulerManager(transmissionManager, clusterManager);
            System.out.println("DAPNETCore started");

            //Wait for Stop
            try {
                System.out.println("Enter \"stop\" to quit");
                Scanner sc = new Scanner(System.in);
                while (true) {
                    if (sc.next().toLowerCase().equals("stop")) {
                        stop();
                        return;
                    }
                }
            } catch (Exception e) {
                //Program was interrupted, not enough time for stopping!
                System.out.println("DAPNET_CORE was interrupted");
            }
        } catch (Exception e) {
            logger.fatal("Exception : ", e);
            System.exit(-1);
        }
    }

    private void stop() {
        logger.info("Stopping DAPNET_CORE...");
        if (clusterManager != null)
            clusterManager.stop();
        if (restManager != null)
            restManager.stopServer();
        if (schedulerManager != null)
            schedulerManager.stop();
        if(clusterManager==null||restManager==null|| schedulerManager ==null)
        {
            //Used for stopping DAPNET while startup
            System.exit(-1);
        }
    }

    // Static
    private static final Logger logger = LogManager.getLogger(DAPNETCore.class.getName());
    private static DAPNETCore dapnetCore;

    public static void main(String[] args) throws Exception {
        //Disable IPv6 for Java VM, creates sometimes LogMessages
        System.setProperty("java.net.preferIPv4Stack", "true");
        //Jersey and Hibernate do not support log4j2, so setting additionally Java Logger to warn level
        setJavaLogLevelToWarn();
        //Set Path to LogSettings
        Configurator.initialize(null, "config/LogSettings.xml");
        System.setProperty("Dlogging.config", "LogSettings.xml");
        //Set language to English
        Locale.getDefault().setDefault(Locale.ENGLISH);

        dapnetCore = new DAPNETCore();
        dapnetCore.start();
    }

    private static void setJavaLogLevelToWarn() {
        java.util.logging.Logger topLogger = java.util.logging.Logger.getLogger("");
        // Handler for console (reuse it if it already exists)
        Handler consoleHandler = null;
        //see if there is already a console handler
        for (Handler handler : topLogger.getHandlers()) {
            if (handler instanceof ConsoleHandler) {
                consoleHandler = handler;
                break;
            }
        }
        if (consoleHandler == null) {
            //no console handler found, create a new one
            consoleHandler = new ConsoleHandler();
            topLogger.addHandler(consoleHandler);
        }
        //set the console handler to fine:
        consoleHandler.setLevel(Level.WARNING);
    }

    public static void stopDAPNETCore() {
        dapnetCore.stop();
    }
}
