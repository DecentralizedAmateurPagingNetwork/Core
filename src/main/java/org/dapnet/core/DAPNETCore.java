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

package org.dapnet.core;

import java.util.Locale;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.cluster.ClusterManager;
import org.dapnet.core.rest.RestManager;
import org.dapnet.core.scheduler.SchedulerManager;
import org.dapnet.core.transmission.Server;
import org.dapnet.core.transmission.TransmissionManager;

public class DAPNETCore {

	private static final Logger logger = LogManager.getLogger(DAPNETCore.class.getName());
	private static final String CORE_VERSION = "1.1.0.0";
	private static final String API_VERSION = "1.1.0";
	private static volatile DAPNETCore dapnetCore;
	private ClusterManager clusterManager;
	private RestManager restManager;
	private TransmissionManager transmissionManager;
	private SchedulerManager schedulerManager;
	private Server deviceServer;

	private void start() {
		try {
			logger.info("Starting DAPNET_CORE Version " + CORE_VERSION + "...");

			logger.info("Starting TransmissionManager");
			transmissionManager = new TransmissionManager();

			logger.info("Starting device server");
			deviceServer = new Server(transmissionManager.getTransmitterManager());
			Thread serverThread = new Thread(deviceServer);
			serverThread.start();

			logger.info("Starting Cluster");
			clusterManager = new ClusterManager(transmissionManager);

			logger.info("Starting RestManager");
			restManager = new RestManager(clusterManager);
			restManager.startServer();

			logger.info("Starting SchedulerManager");
			schedulerManager = new SchedulerManager(transmissionManager, clusterManager);

			logger.info("DAPNETCore started");
		} catch (Exception e) {
			logger.fatal("Exception : ", e);
			System.exit(1);
		}
	}

	private void stop() {
		logger.info("Stopping DAPNET_CORE...");

		if (clusterManager != null) {
			clusterManager.stop();
		}

		if (restManager != null) {
			restManager.stopServer();
		}

		if (schedulerManager != null) {
			schedulerManager.stop();
		}

		try {
			if (deviceServer != null) {
				deviceServer.close();
			}
		} catch (Exception ex) {
			logger.error("Failed to close the device server.", ex);
		}

		// if (clusterManager == null || restManager == null || schedulerManager
		// == null) {
		// // Used for stopping DAPNET while startup
		// System.exit(1);
		// }
	}

	public static void main(String[] args) throws Exception {
		// Disable IPv6 for Java VM, creates sometimes LogMessages
		System.setProperty("java.net.preferIPv4Stack", "true");

		// Jersey and Hibernate do not support log4j2, so setting additionally
		// Java Logger to warn level
		setJavaLogLevelToWarn();

		// Set Path to LogSettings
		// FIXME This won't work...
		// System.setProperty("Dlogging.config", "LogSettings.xml");

		// Set language to English
		Locale.setDefault(Locale.ENGLISH);

		// Register shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					if (dapnetCore != null) {
						dapnetCore.stop();
					}
				} catch (Exception ex) {
					logger.fatal("Exception : ", ex);
				}
			}
		});

		dapnetCore = new DAPNETCore();
		dapnetCore.start();
	}

	private static void setJavaLogLevelToWarn() {
		java.util.logging.Logger topLogger = java.util.logging.Logger.getLogger("");
		// Handler for console (reuse it if it already exists)
		Handler consoleHandler = null;
		// see if there is already a console handler
		for (Handler handler : topLogger.getHandlers()) {
			if (handler instanceof ConsoleHandler) {
				consoleHandler = handler;
				break;
			}
		}
		if (consoleHandler == null) {
			// no console handler found, create a new one
			consoleHandler = new ConsoleHandler();
			topLogger.addHandler(consoleHandler);
		}
		// set the console handler to fine:
		consoleHandler.setLevel(Level.WARNING);
	}

	public static void stopDAPNETCore() {
		dapnetCore.stop();
	}

	public static String getCoreVersion() {
		return CORE_VERSION;
	}

	public static String getApiVersion() {
		return API_VERSION;
	}
}
