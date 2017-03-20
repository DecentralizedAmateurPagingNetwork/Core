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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.dapnet.core.model.validator.ValidName;
import org.dapnet.core.rest.RestAuthorizable;

public class CallSign implements Serializable, RestAuthorizable, Searchable {
	private static final long serialVersionUID = 1884808852367562476L;
	private static volatile State state;
	
	// ID
	@NotNull
	@Size(min = 3, max = 20)
	private String name;

	@NotNull
	@Size(min = 0, max = 60)
	private String description;

	@NotNull
	@Valid
	@Size(min = 1, message = "must contain at least one pager")
	private Collection<Pager> pagers;

	@NotNull
	@Size(min = 1, message = "must contain at least one ownerName")
	private Collection<String> ownerNames;

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

	public Collection<Pager> getPagers() {
		return pagers;
	}

	public void setPagers(Collection<Pager> pagers) {
		this.pagers = pagers;
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

	@ValidName(message = "must contain names of existing users", fieldName = "ownerNames", constraintName = "ValidOwnerNames")
	public Collection<User> getOwners() throws Exception {
		if (ownerNames == null) {
			return null;
		}
		if (state == null) {
			throw new Exception("StateNotSetException");
		}

		ConcurrentMap<String, User> users = state.getUsers();
		ArrayList<User> results = new ArrayList<>();
		for (String owner : ownerNames) {
			User u = users.get(owner.toLowerCase());
			if (u != null)
				results.add(u);
		}

		if (results.size() == ownerNames.size()) {
			return results;
		} else {
			return null;
		}
	}

	@Override
	public String toString() {
		return String.format("CallSign{name='%s'}", name);
	}
}
