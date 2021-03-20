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
import java.util.Date;
import java.util.Set;

import org.dapnet.core.model.validator.RepositoryLookup;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class Activation implements Serializable {
	private static final long serialVersionUID = 1L;

	@NotNull
	@Min(value = 0)
	@Max(value = 2097151)
	private int number;

	@NotNull
	@Size(min = 1, message = "must contain at least one transmitterGroupName")
	@RepositoryLookup(TransmitterGroup.class)
	private Set<String> transmitterGroupNames;

	// Internally set
	@NotNull
	private Date timestamp;

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public Set<String> getTransmitterGroupNames() {
		return transmitterGroupNames;
	}

	public void setTransmitterGroupNames(Set<String> transmitterGroupNames) {
		this.transmitterGroupNames = transmitterGroupNames;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return String.format("Activation{pagerNumber=%07d}", number);
	}
}
