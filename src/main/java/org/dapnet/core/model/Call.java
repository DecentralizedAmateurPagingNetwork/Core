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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.dapnet.core.model.validator.ValidName;

public class Call implements Serializable {
	private static final long serialVersionUID = 1L;
	private static volatile State state;

	// No ID
	@NotNull
	@Size(min = 1, max = 80)
	private String text;

	@NotNull
	@Size(min = 1, message = "must contain at least one callSignName")
	private Set<String> callSignNames;

	@NotNull
	@Size(min = 1, message = "must contain at least one transmitterGroupName")
	private Set<String> transmitterGroupNames;

	// No Validation necessary
	private boolean emergency;

	// Internally set
	@NotNull
	private Instant timestamp;

	// Internally set
	@NotNull
	private String ownerName;

	public Instant getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Set<String> getCallSignNames() {
		return callSignNames;
	}

	public void setCallSignNames(Set<String> callSignNames) {
		this.callSignNames = callSignNames;
	}

	public Set<String> getTransmitterGroupNames() {
		return transmitterGroupNames;
	}

	public void setTransmitterGroupNames(Set<String> transmitterGroupNames) {
		this.transmitterGroupNames = transmitterGroupNames;
	}

	public boolean isEmergency() {
		return emergency;
	}

	public static void setState(State statePar) {
		state = statePar;
	}

	@ValidName(message = "must contain names of existing callSigns", fieldName = "callSignNames", constraintName = "ValidCallSignNames")
	Collection<CallSign> getCallSignsEx() throws Exception {
		if (callSignNames == null) {
			return null;
		}

		if (state == null) {
			throw new Exception("StateNotSetException");
		}

		Map<String, CallSign> map = state.getCallSigns();
		ArrayList<CallSign> result = new ArrayList<>();
		for (String callSign : callSignNames) {
			CallSign s = map.get(callSign.toLowerCase());
			if (s != null) {
				result.add(s);
			}
		}
		if (result.size() == callSignNames.size()) {
			return result;
		} else {
			return null;
		}
	}

	@ValidName(message = "must contain names of existing transmitterGroups", fieldName = "transmitterGroupNames", constraintName = "ValidTransmitterGroupNames")
	Collection<TransmitterGroup> getTransmitterGroups() throws Exception {
		if (transmitterGroupNames == null) {
			return null;
		}

		if (state == null) {
			throw new Exception("StateNotSetException");
		}

		Map<String, TransmitterGroup> groups = state.getTransmitterGroups();
		ArrayList<TransmitterGroup> result = new ArrayList<>();
		for (String transmitterGroup : transmitterGroupNames) {
			TransmitterGroup g = groups.get(transmitterGroup.toLowerCase());
			if (g != null) {
				result.add(g);
			}
		}
		if (transmitterGroupNames.size() == result.size()) {
			return result;
		} else {
			return null;
		}
	}

	@ValidName(message = "must be a name of an existing user", fieldName = "ownerName", constraintName = "ValidOwnerName")
	User getOwner() throws Exception {
		if (state == null) {
			throw new Exception("StateNotSetException");
		}
		if (ownerName != null) {
			return state.getUsers().get(ownerName.toLowerCase());
		} else {
			return null;
		}
	}

	@Override
	public String toString() {
		return String.format("CallSign{name='%s'}", ownerName);
	}
}
