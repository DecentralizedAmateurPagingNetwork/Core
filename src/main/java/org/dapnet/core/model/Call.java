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
import java.util.Set;

import org.dapnet.core.model.validator.RepositoryLookup;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class Call implements Serializable {
	private static final long serialVersionUID = 1L;

	// No ID
	@NotNull
	@Size(min = 1, max = 80)
	private String text;

	@NotNull
	@Size(min = 1, message = "must contain at least one callSignName")
	private Set<@RepositoryLookup(CallSign.class) String> callSignNames;

	@NotNull
	@Size(min = 1, message = "must contain at least one transmitterGroupName")
	private Set<@RepositoryLookup(TransmitterGroup.class) String> transmitterGroupNames;

	// No Validation necessary
	private boolean emergency;

	// Internally set
	@NotNull
	private Instant timestamp;

	// Internally set
	@NotNull
	@RepositoryLookup(User.class)
	private String ownerName;

	public Call() {
	}

	public Call(Call other) {
		if (other == null) {
			throw new NullPointerException("Other object must not be null.");
		}

		text = other.text;
		callSignNames = ModelUtils.copyStringSet(other.callSignNames);
		transmitterGroupNames = ModelUtils.copyStringSet(other.transmitterGroupNames);
		emergency = other.emergency;
		timestamp = other.timestamp;
		ownerName = other.ownerName;
	}

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

	@Override
	public String toString() {
		return String.format("CallSign{name='%s'}", ownerName);
	}
}
