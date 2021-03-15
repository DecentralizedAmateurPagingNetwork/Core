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
import org.dapnet.core.model.StateManager;
import org.dapnet.core.transmission.TransmissionManager;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

public class SchedulerManager {
	private static final Logger logger = LogManager.getLogger();
	private final Scheduler scheduler;

	public SchedulerManager(StateManager stateManager, TransmissionManager transmissionManager,
			ClusterManager clusterManager) throws SchedulerException {
		this.scheduler = StdSchedulerFactory.getDefaultScheduler();

		scheduler.getContext().put("stateManager", stateManager);
		scheduler.getContext().put("transmissionManager", transmissionManager);
		scheduler.getContext().put("clusterManager", clusterManager);
		scheduler.start();

		registerTimeTransmissionJob();
		registerLocalTimeTransmissionJob();
		registerRubricNameTransmissionJob();
		registerNewsTransmissionJob();
		registerStateSavingJob();
		registerStateCleaningJob();
		registerTransmitterIdentificationJob();

		logger.info("SchedulerManager successfully started");
	}

	private void registerTimeTransmissionJob() throws SchedulerException {
		JobDetail job = newJob(TimeTransmissionJob.class).withIdentity("timeTransmissionJob", "main").build();
		CronTrigger trigger = newTrigger().withIdentity("timeTransmissionTrigger", "main")
				.withSchedule(cronSchedule(Settings.getSchedulerSettings().getTimeTransmissionCron())).build();
		scheduler.scheduleJob(job, trigger);
	}

	private void registerLocalTimeTransmissionJob() throws SchedulerException {
		JobDetail job = newJob(LocalTimeTransmissionJob.class).withIdentity("localTimeTransmissionJob", "main").build();
		CronTrigger trigger = newTrigger().withIdentity("localTimeTransmissionTrigger", "main")
				.withSchedule(cronSchedule(Settings.getSchedulerSettings().getLocalTimeTransmissionCron())).build();
		scheduler.scheduleJob(job, trigger);
	}

	private void registerRubricNameTransmissionJob() throws SchedulerException {
		JobDetail job = newJob(RubricNameTransmissionJob.class).withIdentity("rubricNameTransmissionJob", "main")
				.build();
		CronTrigger trigger = newTrigger().withIdentity("rubricNameTransmissionTrigger", "main")
				.withSchedule(cronSchedule(Settings.getSchedulerSettings().getRubricNameTransmissionCron())).build();
		scheduler.scheduleJob(job, trigger);
	}

	private void registerStateSavingJob() throws SchedulerException {
		JobDetail stateSavingJob = newJob(StateSavingJob.class).withIdentity("stateSavingJob", "main").build();

		CronTrigger stateSavingTrigger = newTrigger().withIdentity("stateSavingTrigger", "main")
				.withSchedule(cronSchedule(Settings.getSchedulerSettings().getStateSavingCron())).build();
		scheduler.scheduleJob(stateSavingJob, stateSavingTrigger);
	}

	private void registerStateCleaningJob() throws SchedulerException {
		JobDetail job = newJob(StateCleaningJob.class).withIdentity("stateCleaningJob", "main").build();
		CronTrigger trigger = newTrigger().withIdentity("stateCleaningTrigger", "main")
				.withSchedule(cronSchedule(Settings.getSchedulerSettings().getStateCleaningCron())).build();
		scheduler.scheduleJob(job, trigger);
	}

	private void registerNewsTransmissionJob() throws SchedulerException {
		JobDetail job = newJob(NewsTransmissionJob.class).withIdentity("newsTransmissionJob", "main").build();
		CronTrigger trigger = newTrigger().withIdentity("newsTransmissionTrigger", "main")
				.withSchedule(cronSchedule(Settings.getSchedulerSettings().getNewsTransmissionCron())).build();
		scheduler.scheduleJob(job, trigger);
	}

	private void registerTransmitterIdentificationJob() throws SchedulerException {
		JobDetail job = newJob(TransmitterIdentificationJob.class).withIdentity("transmitterIdentificationJob", "main")
				.build();
		CronTrigger trigger = newTrigger().withIdentity("transmitterIdentificationTrigger", "main")
				.withSchedule(cronSchedule(Settings.getSchedulerSettings().getTransmitterIdentificationCron())).build();
		scheduler.scheduleJob(job, trigger);
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
