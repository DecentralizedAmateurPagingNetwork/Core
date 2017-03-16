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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.cluster.ClusterSettings;
import org.dapnet.core.model.ModelSettings;
import org.dapnet.core.rest.RestSettings;
import org.dapnet.core.scheduler.SchedulerSettings;
import org.dapnet.core.transmission.TransmissionSettings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@XmlRootElement
public class Settings implements Serializable {
	private static final long serialVersionUID = 937400690804047335L;
	private static final Logger logger = LogManager.getLogger();
	private static volatile Settings settings;

	private TransmissionSettings transmissionSettings;
	private ModelSettings modelSettings;
	private RestSettings restSettings;
	private ClusterSettings clusterSettings;
	private SchedulerSettings schedulerSettings;

	private Settings() {
		transmissionSettings = new TransmissionSettings();
		modelSettings = new ModelSettings();
		restSettings = new RestSettings();
		clusterSettings = new ClusterSettings();
		schedulerSettings = new SchedulerSettings();
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

	private static Settings getSettings() {
		if (settings == null) {
			try {
				String filename = System.getProperty("dapnet.core.settings", "config/Settings.json");
				settings = new Gson().fromJson(readFile(filename), Settings.class);
			} catch (Exception e) {
				logger.warn("Creating new settings file");
				settings = createDefaultSettings();
			}
		}
		return settings;
	}

	private static Settings createDefaultSettings() {
		settings = new Settings();
		try {
			File file = new File("config/Settings.json");
			FileWriter writer = new FileWriter(file);
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			writer.write(gson.toJson(settings));
			writer.flush();
			writer.close();
		} catch (IOException e) {
			logger.error("Failed to create settings file. Using default values", e);
		}
		return settings;
	}
}
