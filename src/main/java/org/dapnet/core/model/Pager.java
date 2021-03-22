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

import org.dapnet.core.model.validator.PagerAddress;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class Pager implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Enumeration of supported pager types.
	 * 
	 * @author Philipp Thiel
	 */
	public enum Type {
		SKYPER, ALPHAPOC, SWISSPHONE, QUIX_ULTRA
	}

	@NotNull
	private Type type = Type.SKYPER;

	@PagerAddress(checkDuplicates = false)
	private int number;

	@NotNull
	@Size(min = 3, max = 20)
	private String name;

	public Pager() {
	}

	public Pager(Pager other) {
		if (other == null) {
			throw new NullPointerException("Other object must not be null.");
		}

		type = other.type;
		number = other.number;
		name = other.name;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return String.format("Pager{number=%07d, name='%s'}", number, name);
	}
}
