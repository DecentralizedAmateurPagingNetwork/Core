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

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * This class contains the actual state that can also be serialized.
 * 
 * @author Philipp Thiel
 */
final class State implements Serializable {

	private static final long serialVersionUID = 1L;

	@NotNull(message = "nicht vorhanden")
	@Valid
	private Map<String, CallSign> callSigns = new TreeMap<>();

	@NotNull(message = "nicht vorhanden")
	@Valid
	private Map<String, Node> nodes = new TreeMap<>();

	@NotNull(message = "nicht vorhanden")
	@Valid
	private Map<String, User> users = new TreeMap<>();

	@NotNull(message = "nicht vorhanden")
	@Valid
	private Collection<Call> calls = new LinkedList<>();

	@NotNull(message = "nicht vorhanden")
	@Valid
	private Map<String, Transmitter> transmitters = new TreeMap<>();

	@NotNull(message = "nicht vorhanden")
	@Valid
	private Map<String, TransmitterGroup> transmitterGroups = new TreeMap<>();

	@NotNull(message = "nicht vorhanden")
	@Valid
	private Map<String, Rubric> rubrics = new TreeMap<>();

	@NotNull(message = "nicht vorhande")
	@Valid
	private Map<String, NewsList> news = new TreeMap<>();

	@NotNull
	@Valid
	private CoreStatistics stats = new CoreStatistics();

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
