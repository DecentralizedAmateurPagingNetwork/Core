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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.dapnet.core.model.validator.ValidName;

public class Activation implements Serializable {
	private static final long serialVersionUID = 2653692303199008619L;
	private static volatile State state;

	@NotNull
	@Min(value = 0)
	@Max(value = 2097151)
	private int number;

	@NotNull
	@Size(min = 1, message = "must contain at least one transmitterGroupName")
	private List<String> transmitterGroupNames;

	// Internally set
	@NotNull
	private Date timestamp;

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public List<String> getTransmitterGroupNames() {
		return transmitterGroupNames;
	}

	public void setTransmitterGroupNames(List<String> transmitterGroupNames) {
		this.transmitterGroupNames = transmitterGroupNames;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public static void setState(State statePar) {
		state = statePar;
	}

	@ValidName(message = "must contain names of existing transmitterGroups", fieldName = "transmitterGroupNames", constraintName = "ValidTransmitterGroupNames")
	public ArrayList<TransmitterGroup> getTransmitterGroups() throws Exception {
		if (state == null) {
			throw new Exception("StateNotSetException");
		}

		if (transmitterGroupNames == null) {
			return null;
		}

		ArrayList<TransmitterGroup> transmitterGroups = new ArrayList<>();

		for (String transmitterGroup : transmitterGroupNames) {
			TransmitterGroup g = state.getTransmitterGroups().get(transmitterGroup);
			if (g != null) {
				transmitterGroups.add(g);
			}
		}
		if (transmitterGroups.size() == transmitterGroups.size()) {
			return transmitterGroups;
		} else {
			return null;
		}
	}

	@Override
	public String toString() {
		return String.format("Activation{pagerNumber=%07d}", number);
	}
}
