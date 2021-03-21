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
import java.util.Set;

import org.dapnet.core.model.validator.RepositoryLookup;
import org.jgroups.stack.IpAddress;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class Node implements Serializable, NamedObject {
	private static final long serialVersionUID = 1L;

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
	private Set<@RepositoryLookup(User.class) String> ownerNames;

	private String version;

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

	public Node(Node other) {
		if (other == null) {
			throw new NullPointerException("Other object must not be null.");
		}

		name = other.name;
		longitude = other.longitude;
		latitude = other.latitude;
		address = other.address != null ? other.address.copy() : null;
		status = other.status;
		ownerNames = ModelUtils.copyStringSet(other.ownerNames);
		version = other.version;
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

	public Set<String> getOwnerNames() {
		return ownerNames;
	}

	public void setOwnerNames(Set<String> owners) {
		this.ownerNames = owners;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return String.format("Node{status=%s, name='%s'}", status, name);
	}
}
