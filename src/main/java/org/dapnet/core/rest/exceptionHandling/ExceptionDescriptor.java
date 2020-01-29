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

package org.dapnet.core.rest.exceptionHandling;

import java.io.Serializable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ExceptionDescriptor implements Serializable {
	private static final long serialVersionUID = 7205168715512923074L;
	protected static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	protected int code;
	protected String name;
	protected String message;

	public ExceptionDescriptor(int code, String name, String message) {
		this.code = code;
		this.name = name;
		this.message = message;
	}

	public String toJson() {
		return gson.toJson(this);
	}

	public String getLogMessage() {
		return name + " (" + code + ")";
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
