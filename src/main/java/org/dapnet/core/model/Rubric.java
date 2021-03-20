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

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class Rubric implements Serializable, RestAuthorizable, NamedObject {
	private static final long serialVersionUID = 1L;

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
	@RepositoryLookup(TransmitterGroup.class)
	private Set<String> transmitterGroupNames;

	@NotNull
	@Size(min = 1, max = 11)
	private String label;

	@NotNull
	@Size(min = 1, message = "must contain at least one ownerName")
	@RepositoryLookup(User.class)
	private Set<String> ownerNames;

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public int getAddress() {
		return 1000 + number;
	}

	public Set<String> getTransmitterGroupNames() {
		return transmitterGroupNames;
	}

	public void setTransmitterGroupNames(Set<String> transmitterGroupNames) {
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
	public Set<String> getOwnerNames() {
		return ownerNames;
	}

	public void setOwnerNames(Set<String> owners) {
		this.ownerNames = owners;
	}

	@Override
	public String toString() {
		return String.format("Rubric{name='%s', number=%d}", name, number);
	}
}
