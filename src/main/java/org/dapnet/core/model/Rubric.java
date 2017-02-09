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
import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.dapnet.core.model.validator.ValidName;
import org.dapnet.core.rest.RestAuthorizable;

public class Rubric implements Serializable, RestAuthorizable, Searchable {
	private static final long serialVersionUID = 6724514122275380520L;

	// ID
	@NotNull
	@Size(min = 3, max = 20)
	private String name;

	@NotNull
	@Min(value = 1)
	@Max(value = 95)
	private int number;

	@NotNull
	@Size(min = 1, message = "must contain at least one transmitterGroupName")
	private List<String> transmitterGroupNames;

	@NotNull
	@Size(min = 1, max = 11)
	private String label;

	@NotNull
	@Size(min = 1, message = "must contain at least one ownerName")
	private ArrayList<String> ownerNames;

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public ArrayList<String> getOwnerNames() {
		return ownerNames;
	}

	public void setOwnerNames(ArrayList<String> owners) {
		this.ownerNames = owners;
	}

	// Getter returning references instead of String
	private static State state;

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

	@ValidName(message = "must contain names of existing users", fieldName = "ownerNames", constraintName = "ValidOwnerNames")
	public ArrayList<User> getOwners() throws Exception {
		if (state == null) {
			throw new Exception("StateNotSetException");
		}

		if (ownerNames == null) {
			return null;
		}

		ArrayList<User> users = new ArrayList<>();
		for (String owner : ownerNames) {
			User u = state.getUsers().get(owner);
			if (u != null) {
				users.add(u);
			}
		}

		if (ownerNames.size() == users.size()) {
			return users;
		} else {
			return null;
		}
	}

	@Override
	public String toString() {
		return String.format("Rubric{name='%s', number=%d}", name, number);
	}
}
