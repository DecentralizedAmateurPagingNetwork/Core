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

package org.dapnet.core.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.Settings;
import org.dapnet.core.rest.GsonTypeAdapterFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class State implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger();
	private static final Gson gson;
	private static final ReadWriteLock lock = new ReentrantReadWriteLock();

	static {
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		builder.registerTypeAdapterFactory(new GsonTypeAdapterFactory());
		gson = builder.create();
	}

	@NotNull(message = "nicht vorhanden")
	@Valid
	private Map<String, CallSign> callSigns = new HashMap<>();

	@NotNull(message = "nicht vorhanden")
	@Valid
	private Map<String, Node> nodes = new HashMap<>();

	@NotNull(message = "nicht vorhanden")
	@Valid
	private Map<String, User> users = new HashMap<>();

	@NotNull(message = "nicht vorhanden")
	@Valid
	private Collection<Call> calls;

	@NotNull(message = "nicht vorhanden")
	@Valid
	private Map<String, Transmitter> transmitters = new HashMap<>();

	@NotNull(message = "nicht vorhanden")
	@Valid
	private Map<String, TransmitterGroup> transmitterGroups = new HashMap<>();

	@NotNull(message = "nicht vorhanden")
	@Valid
	private Map<String, Rubric> rubrics = new HashMap<>();

	@NotNull(message = "nicht vorhande")
	@Valid
	private Map<String, NewsList> news = new HashMap<>();

	@NotNull
	@Valid
	private CoreStatistics stats = new CoreStatistics();

	public State() {
		calls = Collections.synchronizedList(new ArrayList<>());

		setModelReferences();
	}

	public static Lock getReadLock() {
		return lock.readLock();
	}

	public static Lock getWriteLock() {
		return lock.writeLock();
	}

	public void setModelReferences() {
		// Setting reference to state in model for allow returning of reference
		// instead of strings
		Activation.setState(this);
		Call.setState(this);
		CallSign.setState(this);
		News.setState(this);
		Node.setState(this);
		Rubric.setState(this);
		Transmitter.setState(this);
		TransmitterGroup.setState(this);
	}

	public static State readFromFile() throws Exception {
		lock.writeLock().lock();
		try (InputStreamReader reader = new InputStreamReader(
				new FileInputStream(Settings.getModelSettings().getStateFile()), "UTF-8")) {
			return gson.fromJson(reader, State.class);
		} finally {
			lock.writeLock().unlock();
		}
	}

	public void writeToFile() {
		File file = new File(Settings.getModelSettings().getStateFile());
		try {
			if (file.getParentFile() != null) {
				file.getParentFile().mkdirs();
			}

			lock.readLock().lock();
			try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8")) {
				writer.write(gson.toJson(this));
				writer.flush();
			} finally {
				lock.readLock().unlock();
			}

			logger.info("Successfully wrote state to file");
		} catch (Exception e) {
			logger.fatal("Failed to write state file: ", e);
		}
	}

	public Collection<Call> getCalls() {
		return calls;
	}

	public Map<String, CallSign> getCallSigns() {
		return callSigns;
	}

	public Map<String, Node> getNodes() {
		return nodes;
	}

	public Map<String, User> getUsers() {
		return users;
	}

	public Map<String, Transmitter> getTransmitters() {
		return transmitters;
	}

	public Map<String, TransmitterGroup> getTransmitterGroups() {
		return transmitterGroups;
	}

	public Map<String, Rubric> getRubrics() {
		return rubrics;
	}

	public Map<String, NewsList> getNews() {
		return news;
	}

	public CoreStatistics getStatistics() {
		return stats;
	}

}
