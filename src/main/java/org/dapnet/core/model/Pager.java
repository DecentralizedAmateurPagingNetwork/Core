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

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

public class Pager implements Serializable {
	private static final long serialVersionUID = -8518189973413053726L;

	// No ID
	@NotNull
	@Min(value = 0)
	@Max(value = 2097151)
	int number;

	@NotNull
	@Size(min = 3, max = 20)
	String name;

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
		// TODO Pad with zeros
		return String.format("Pager{number=%d, name=\'%s\'}", number, name);
	}
}
