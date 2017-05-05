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

import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.dapnet.core.model.validator.ValidName;
import org.jgroups.stack.IpAddress;

public class Node implements Serializable, Searchable {
	private static final long serialVersionUID = -4104175163845286560L;
	private static volatile State state;

	// ID
	@NotNull
	@Size(min = 3, max = 20)
	private String name;

	@NotNull
	@Digits(integer = 3, fraction = 8)
	@Min(-180)
	@Max(+180)
	private String longitude;

	@NotNull
	@Digits(integer = 3, fraction = 8)
	@Min(-90)
	@Max(+90)
	private String latitude;

	private IpAddress address;

	@NotNull
	private Status status;

	@NotNull
	@Size(min = 1, message = "must contain at least one ownerName")
	private Collection<String> ownerNames;

	public enum Status {
		ONLINE, SUSPENDED, UNKNOWN
	}

	public Node(String name, IpAddress address, String longitude, String latitude, Status status) {
		this.name = name;
		this.longitude = longitude;
		this.latitude = latitude;
		this.address = address;
		this.status = status;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public IpAddress getAddress() {
		return address;
	}

	public void setAddress(IpAddress address) {
		this.address = address;
	}

	public Collection<String> getOwnerNames() {
		return ownerNames;
	}

	public void setOwnerNames(Collection<String> owners) {
		this.ownerNames = owners;
	}

	@Override
	public String toString() {
		return String.format("Node{status=%s, name='%s'}", status, name);
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

	public static State getState() {
		return state;
	}

	public static void setState(State statePar) {
		state = statePar;
	}
}
