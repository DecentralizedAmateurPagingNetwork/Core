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

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class Pager implements Serializable {
	private static final long serialVersionUID = -8518189973413053726L;

	@NotNull
	@Min(value = 0)
	@Max(value = 2097151, message = "pager number is limited to 21 bits")
	private int number;

	@NotNull
	@Size(min = 3, max = 20)
	private String name;

	@Min(value = 0, message = "subric must be in range 0-3")
	@Max(value = 3, message = "subric must be in range 0-3")
	private Integer subric;

	public Integer getSubric() {
		return subric;
	}

	public void setSubric(Integer subric) {
		this.subric = subric;
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
