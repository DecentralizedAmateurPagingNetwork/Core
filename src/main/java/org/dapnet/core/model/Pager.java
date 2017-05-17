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

	@NotNull
	@Min(value = 0)
	@Max(value = 2097151, message = "pager number is limited to 21 bits")
	private int number;

	@NotNull
	@Size(min = 3, max = 20)
	private String name;

	@NotNull
	private boolean numeric = false;

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

	public boolean isNumeric() {
		return numeric;
	}

	public void setNumeric(boolean numeric) {
		this.numeric = numeric;
	}

	@Override
	public String toString() {
		return String.format("Pager{number=%07d, name='%s', numeric=%b}", number, name, numeric);
	}
}
