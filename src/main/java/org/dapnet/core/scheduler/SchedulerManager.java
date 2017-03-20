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

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.Settings;
import org.dapnet.core.cluster.ClusterManager;
import org.dapnet.core.transmission.TransmissionManager;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

public class SchedulerManager {
	private static final Logger logger = LogManager.getLogger();
	private final Scheduler scheduler;

	public SchedulerManager(TransmissionManager transmissionManager, ClusterManager clusterManager)
			throws SchedulerException {
		this.scheduler = StdSchedulerFactory.getDefaultScheduler();

		scheduler.getContext().put("transmissionManager", transmissionManager);
		scheduler.getContext().put("clusterManager", clusterManager);
		scheduler.start();

		registerTimeTransmissionJob();
		registerRubricNameTransmissionJob();
		registerNewsTransmissionJob();
		registerStateSavingJob();
		registerStateCleaningJob();

		logger.info("SchedulerManager successfully started");
	}

	private void registerTimeTransmissionJob() throws SchedulerException {
		JobDetail timeTransmissionJob = newJob(TimeTransmissionJob.class).withIdentity("timeTransmissionJob", "main")
				.build();

		CronTrigger timeTransmissionTrigger = newTrigger().withIdentity("timeTransmissionTrigger", "main")
				.withSchedule(cronSchedule(Settings.getSchedulerSettings().getTimeTransmissionCron())).build();
		scheduler.scheduleJob(timeTransmissionJob, timeTransmissionTrigger);
	}

	private void registerRubricNameTransmissionJob() throws SchedulerException {
		JobDetail rubricNameTransmissionJob = newJob(RubricNameTransmissionJob.class)
				.withIdentity("rubricNameTransmissionJob", "main").build();

		CronTrigger rubricNameTransmissionTrigger = newTrigger().withIdentity("rubricNameTransmissionTrigger", "main")
				.withSchedule(cronSchedule(Settings.getSchedulerSettings().getRubricNameTransmissionCron())).build();
		scheduler.scheduleJob(rubricNameTransmissionJob, rubricNameTransmissionTrigger);
	}

	private void registerStateSavingJob() throws SchedulerException {
		JobDetail stateSavingJob = newJob(StateSavingJob.class).withIdentity("stateSavingJob", "main").build();

		CronTrigger stateSavingTrigger = newTrigger().withIdentity("stateSavingTrigger", "main")
				.withSchedule(cronSchedule(Settings.getSchedulerSettings().getStateSavingCron())).build();
		scheduler.scheduleJob(stateSavingJob, stateSavingTrigger);
	}

	private void registerStateCleaningJob() throws SchedulerException {
		JobDetail stateCleaningJob = newJob(StateCleaningJob.class).withIdentity("stateCleaningJob", "main").build();

		CronTrigger stateCleaningTrigger = newTrigger().withIdentity("stateCleaningTrigger", "main")
				.withSchedule(cronSchedule(Settings.getSchedulerSettings().getStateCleaningCron())).build();
		scheduler.scheduleJob(stateCleaningJob, stateCleaningTrigger);
	}

	private void registerNewsTransmissionJob() throws SchedulerException {
		JobDetail newsTransmissionJob = newJob(NewsTransmissionJob.class).withIdentity("newsTransmissionJob", "main")
				.build();

		CronTrigger newsTransmissionTrigger = newTrigger().withIdentity("newsTransmissionTrigger", "main")
				.withSchedule(cronSchedule(Settings.getSchedulerSettings().getNewsTransmissionCron())).build();
		scheduler.scheduleJob(newsTransmissionJob, newsTransmissionTrigger);
	}

	public void stop() {
		try {
			scheduler.shutdown();
			logger.info("SchedulerManager successfully stopped");
		} catch (SchedulerException e) {
			logger.error("Failed to stop Scheduler:", e);
		}
	}

}
