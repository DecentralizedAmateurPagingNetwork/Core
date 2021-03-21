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
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import org.dapnet.core.model.validator.RepositoryLookup;
import org.dapnet.core.rest.RestAuthorizable;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CallSign implements Serializable, RestAuthorizable, NamedObject {
	private static final long serialVersionUID = 1L;

	// ID
	@NotNull
	@Size(min = 3, max = 20)
	private String name;

	@NotNull
	@Size(min = 0, max = 60)
	private String description;

	@NotNull
	private boolean numeric = false;

	@NotNull
	@Valid
	@Size(min = 1, message = "Must contain at least one pager")
	private Collection<Pager> pagers;

	@NotNull
	@Size(min = 1, message = "Must contain at least one ownerName")
	private Set<@RepositoryLookup(User.class) String> ownerNames;

	public CallSign() {
	}

	public CallSign(CallSign other) {
		if (other == null) {
			throw new NullPointerException("Other object must not be null.");
		}

		name = other.name;
		description = other.description;
		numeric = other.numeric;
		pagers = copyPagers(other.pagers);
		ownerNames = ModelUtils.copyStringSet(other.ownerNames);
	}

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

	public boolean isNumeric() {
		return numeric;
	}

	public void setNumeric(boolean numeric) {
		this.numeric = numeric;
	}

	public Collection<Pager> getPagers() {
		return pagers;
	}

	public void setPagers(Collection<Pager> pagers) {
		this.pagers = pagers;
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
		return String.format("CallSign{name='%s'}", name);
	}

	private static Collection<Pager> copyPagers(Collection<Pager> src) {
		if (src == null) {
			return null;
		}

		Collection<Pager> result = new LinkedList<>();
		src.forEach(p -> result.add(new Pager(p)));

		return result;
	}
}
