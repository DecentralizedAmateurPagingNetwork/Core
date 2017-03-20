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

import static org.jgroups.util.Util.readFile;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.cluster.ClusterSettings;
import org.dapnet.core.model.ModelSettings;
import org.dapnet.core.rest.RestSettings;
import org.dapnet.core.scheduler.SchedulerSettings;
import org.dapnet.core.transmission.TransmissionSettings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Settings implements Serializable {
	private static final long serialVersionUID = 937400690804047335L;
	private static final Logger logger = LogManager.getLogger();
	private static Settings settings;

	private final TransmissionSettings transmissionSettings = new TransmissionSettings();
	private final ModelSettings modelSettings = new ModelSettings();
	private final RestSettings restSettings = new RestSettings();
	private final ClusterSettings clusterSettings = new ClusterSettings();
	private final SchedulerSettings schedulerSettings = new SchedulerSettings();

	private Settings() {
	}

	public static TransmissionSettings getTransmissionSettings() {
		return getSettings().transmissionSettings;
	}

	public static ModelSettings getModelSettings() {
		return getSettings().modelSettings;
	}

	public static RestSettings getRestSettings() {
		return getSettings().restSettings;
	}

	public static ClusterSettings getClusterSettings() {
		return getSettings().clusterSettings;
	}

	public static SchedulerSettings getSchedulerSettings() {
		return getSettings().schedulerSettings;
	}

	private static synchronized Settings getSettings() {
		if (settings == null) {
			try {
				String filename = System.getProperty("dapnet.core.settings", "config/Settings.json");
				settings = new Gson().fromJson(readFile(filename), Settings.class);
			} catch (Exception e) {
				logger.warn("Creating new settings file.");
				createDefaultSettings();
			}
		}
		return settings;
	}

	private static void createDefaultSettings() {
		settings = new Settings();

		String filename = System.getProperty("dapnet.core.settings", "config/Settings.json");
		try (FileWriter writer = new FileWriter(filename)) {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			writer.write(gson.toJson(settings));
		} catch (IOException e) {
			logger.error("Failed to create settings file.", e);
		}
	}
}
