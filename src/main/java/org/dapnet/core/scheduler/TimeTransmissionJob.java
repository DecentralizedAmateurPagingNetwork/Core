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

import java.time.ZonedDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.transmission.TransmissionManager;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;

public class TimeTransmissionJob implements Job {
	private static final Logger logger = LogManager.getLogger();

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {

		SchedulerContext schedulerContext = null;
		try {
			schedulerContext = context.getScheduler().getContext();
			TransmissionManager transmissionManager = (TransmissionManager) schedulerContext.get("transmissionManager");

			// TODO We could also add a time zone to the transmitter/transmitter group and
			// send out local time?
			ZonedDateTime now = ZonedDateTime.now();
			transmissionManager.sendTime(now);
		} catch (SchedulerException e) {
			logger.error("Failed to execute TimeTransmissionJob", e);
		}
	}
}
