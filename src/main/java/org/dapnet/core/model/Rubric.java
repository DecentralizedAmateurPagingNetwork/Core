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
import java.util.Collection;
import java.util.concurrent.ConcurrentMap;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.dapnet.core.model.validator.ValidName;
import org.dapnet.core.rest.RestAuthorizable;

public class Rubric implements Serializable, RestAuthorizable, Searchable {
	private static final long serialVersionUID = 6724514122275380520L;
	private static volatile State state;

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
	private Collection<String> transmitterGroupNames;

	@NotNull
	@Size(min = 1, max = 11)
	private String label;

	@NotNull
	@Size(min = 1, message = "must contain at least one ownerName")
	private Collection<String> ownerNames;

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public int getAddress() {
		return 1000 + number;
	}

	public Collection<String> getTransmitterGroupNames() {
		return transmitterGroupNames;
	}

	public void setTransmitterGroupNames(Collection<String> transmitterGroupNames) {
		this.transmitterGroupNames = transmitterGroupNames;
	}

	@Override
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

	@Override
	public Collection<String> getOwnerNames() {
		return ownerNames;
	}

	public void setOwnerNames(Collection<String> owners) {
		this.ownerNames = owners;
	}

	public static void setState(State statePar) {
		state = statePar;
	}

	@ValidName(message = "must contain names of existing transmitterGroups", fieldName = "transmitterGroupNames", constraintName = "ValidTransmitterGroupNames")
	public Collection<TransmitterGroup> getTransmitterGroups() throws Exception {
		if (state == null) {
			throw new Exception("StateNotSetException");
		}
		if (transmitterGroupNames == null) {
			return null;
		}

		ConcurrentMap<String, TransmitterGroup> groups = state.getTransmitterGroups();
		ArrayList<TransmitterGroup> result = new ArrayList<>();
		for (String transmitterGroup : transmitterGroupNames) {
			TransmitterGroup g = groups.get(transmitterGroup.toLowerCase());
			if (g != null) {
				result.add(g);
			}
		}

		if (result.size() == result.size()) {
			return result;
		} else {
			return null;
		}
	}

	@ValidName(message = "must contain names of existing users", fieldName = "ownerNames", constraintName = "ValidOwnerNames")
	public Collection<User> getOwners() throws Exception {
		if (state == null) {
			throw new Exception("StateNotSetException");
		}

		if (ownerNames == null) {
			return null;
		}

		ConcurrentMap<String, User> users = state.getUsers();
		ArrayList<User> results = new ArrayList<>();
		for (String owner : ownerNames) {
			User u = users.get(owner.toLowerCase());
			if (u != null) {
				results.add(u);
			}
		}

		if (ownerNames.size() == results.size()) {
			return results;
		} else {
			return null;
		}
	}

	@Override
	public String toString() {
		return String.format("Rubric{name='%s', number=%d}", name, number);
	}
}
