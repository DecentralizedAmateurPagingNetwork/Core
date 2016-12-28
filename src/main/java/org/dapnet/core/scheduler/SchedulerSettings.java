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

package org.dapnet.core.scheduler;

import java.io.Serializable;

public class SchedulerSettings implements Serializable {
	private static final long serialVersionUID = -8720953587062509265L;
	private String timeTransmissionCron = "0 0/20 * * * ?";
	private String rubricNameTransmissionCron = "15 0/20 * * * ?";
	private String stateSavingCron = "30 0/10 * * * ?";
	private String stateCleaningCron = "0 0 0 * * ?";

	public String getTimeTransmissionCron() {
		return timeTransmissionCron;
	}

	public String getRubricNameTransmissionCron() {
		return rubricNameTransmissionCron;
	}

	public String getStateSavingCron() {
		return stateSavingCron;
	}

	public String getStateCleaningCron() {
		return stateCleaningCron;
	}
}
