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
import java.time.Instant;

import org.dapnet.core.model.validator.RepositoryLookup;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class News implements Serializable {
	private static final long serialVersionUID = 1L;
	private static volatile State state;

	// No ID
	@NotNull
	@Size(min = 1, max = 80)
	private String text;

	@NotNull
	@RepositoryLookup(Rubric.class)
	private String rubricName;

	@NotNull
	@Min(value = 0)
	@Max(value = 10)
	private int number;

	// Internally set
	@NotNull
	private Instant timestamp;

	// Internally set
	@NotNull
	@Size(min = 1, message = "must contain at least one ownerName")
	@RepositoryLookup(User.class)
	private String ownerName;

	public News() {
	}

	public News(News other) {
		if (other == null) {
			throw new NullPointerException("Other object must not be null.");
		}

		text = other.text;
		rubricName = other.rubricName;
		number = other.number;
		timestamp = other.timestamp;
		ownerName = other.ownerName;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getRubricName() {
		return rubricName;
	}

	public void setRubricName(String rubricName) {
		this.rubricName = rubricName;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public static State getState() {
		return state;
	}

	public static void setState(State statePar) {
		state = statePar;
	}

	@Override
	public String toString() {
		return String.format("News{rubricName='%s', number=%d}", rubricName, number);
	}
}
