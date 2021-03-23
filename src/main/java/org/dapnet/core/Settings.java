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
	private static final Logger logger = LogManager.getLogger();
	private static final long serialVersionUID = 1L;
	private TransmissionSettings transmissionSettings = new TransmissionSettings();
	private ModelSettings modelSettings = new ModelSettings();
	private RestSettings restSettings = new RestSettings();
	private ClusterSettings clusterSettings = new ClusterSettings();
	private SchedulerSettings schedulerSettings = new SchedulerSettings();

	private Settings() {
	}

	public TransmissionSettings getTransmissionSettings() {
		return transmissionSettings;
	}

	public ModelSettings getModelSettings() {
		return modelSettings;
	}

	public RestSettings getRestSettings() {
		return restSettings;
	}

	public ClusterSettings getClusterSettings() {
		return clusterSettings;
	}

	public SchedulerSettings getSchedulerSettings() {
		return schedulerSettings;
	}

	public static Settings loadFromFile(String fileName) {
		if (fileName == null) {
			throw new NullPointerException("File name must not be null.");
		}

		final Gson gson = new GsonBuilder().setPrettyPrinting().create();
		Settings settings = null;

		try {
			settings = gson.fromJson(readFile(fileName), Settings.class);
		} catch (Exception e) {
			logger.warn("Creating new settings file.");
			settings = createDefaultSettings(fileName, gson);
		}

		return settings;
	}

	private static Settings createDefaultSettings(String fileName, Gson gson) {
		Settings settings = new Settings();

		try (FileWriter writer = new FileWriter(fileName)) {
			writer.write(gson.toJson(settings));
		} catch (IOException e) {
			logger.error("Failed to save settings file.", e);
		}

		return settings;
	}

}
