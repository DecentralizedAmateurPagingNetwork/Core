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
import org.dapnet.core.rest.RestAuthorizable;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TransmitterGroup implements Serializable, RestAuthorizable, NamedObject {
	private static final long serialVersionUID = 1L;

	// ID
	@NotNull
	@Size(min = 3, max = 20)
	private String name;

	@NotNull
	@Size(min = 0, max = 60)
	private String description;

	@NotNull
	@Size(min = 1, message = "must contain at least one transmitterName")
	@RepositoryLookup(Transmitter.class)
	private Set<String> transmitterNames;

	@NotNull
	@Size(min = 1, message = "must contain at least one ownerName")
	@RepositoryLookup(User.class)
	private Set<String> ownerNames;

	@Override
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

	public Set<String> getTransmitterNames() {
		return transmitterNames;
	}

	public void setTransmitterNames(Set<String> transmitterNames) {
		this.transmitterNames = transmitterNames;
	}

	@Override
	public Set<String> getOwnerNames() {
		return ownerNames;
	}

	public void setOwnerNames(Set<String> owners) {
		this.ownerNames = owners;
	}

	@Override
	public String toString() {
		return String.format("TransmitterGroup{name='%s'}", name);
	}
}
