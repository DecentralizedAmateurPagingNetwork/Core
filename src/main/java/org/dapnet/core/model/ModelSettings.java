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

public class ModelSettings implements Serializable {
	private static final long serialVersionUID = 1L;
	private String stateFile = "data/State.json";
	private boolean savingImmediately = false;
	private long callExpirationTimeInMinutes = 24 * 60;
	private long newsExpirationTimeInMinutes = 24 * 60;
	private long transmitterExpirationDays = 60;

	public String getStateFile() {
		return stateFile;
	}

	public boolean isSavingImmediately() {
		return savingImmediately;
	}

	public long getCallExpirationTimeInMinutes() {
		return callExpirationTimeInMinutes;
	}

	public long getNewsExpirationTimeInMinutes() {
		return newsExpirationTimeInMinutes;
	}

	public long getTransmitterExpirationDays() {
		return transmitterExpirationDays;
	}
}
