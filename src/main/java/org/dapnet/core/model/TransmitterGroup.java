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

import org.dapnet.core.model.list.Searchable;
import org.dapnet.core.model.validator.ValidName;
import org.dapnet.core.rest.RestAuthorizable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;

public class TransmitterGroup implements Serializable, RestAuthorizable, Searchable {
	private static final long serialVersionUID = 2641698714366327412L;

	// ID
	@NotNull
	@Size(min = 3, max = 20)
	private String name;

	@NotNull
	@Size(min = 0, max = 60)
	private String description;

	@NotNull
	@Size(min = 1, message = "must contain at least one transmitterName")
	private ArrayList<String> transmitterNames;

	@NotNull
	@Size(min = 1, message = "must contain at least one ownerName")
	private ArrayList<String> ownerNames;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ArrayList<String> getTransmitterNames() {
		return transmitterNames;
	}

	public void setTransmitterNames(ArrayList<String> transmitterNames) {
		this.transmitterNames = transmitterNames;
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

	@ValidName(message = "must contain names of existing users", fieldName = "ownerNames", constraintName = "ValidOwnerNames")
	public ArrayList<User> getOwners() throws Exception {
		if (state == null)
			throw new Exception("StateNotSetException");
		ArrayList<User> users = new ArrayList<>();
		if (ownerNames == null)
			return null;
		for (String owner : ownerNames) {
			if (state.getUsers().contains(owner))
				users.add(state.getUsers().findByName(owner));
		}
		if (ownerNames.size() == users.size())
			return users;
		else
			return null;
	}

	@ValidName(message = "must contain names of existing transmitters", fieldName = "transmitterNames", constraintName = "ValidTransmitterNames")
	public ArrayList<Transmitter> getTransmitter() throws Exception {
		if (state == null)
			throw new Exception("StateNotSetException");
		ArrayList<Transmitter> transmitters = new ArrayList<>();
		if (transmitterNames == null)
			return null;
		for (String transmitterName : transmitterNames) {
			if (state.getTransmitters().contains(transmitterName)) {
				transmitters.add(state.getTransmitters().findByName(transmitterName));
			}
		}
		if (transmitters.size() == transmitterNames.size())
			return transmitters;
		else
			return null;
	}

	public boolean contains(String transmitter) {
		for (String transmitterName : transmitterNames) {
			if (transmitter.equals(transmitterName))
				return true;
		}
		return false;
	}

	public boolean contains(Transmitter transmitter) {
		return contains(transmitter.getName());
	}

	@Override
	public String toString() {
		return "TransmitterGroup{" + "name='" + name + '\'' + '}';
	}
}
