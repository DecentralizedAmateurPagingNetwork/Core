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

import java.util.Objects;

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

/**
 * The scheduler manager is responsible for running scheduled tasks.
 * 
 * @author Philipp Thiel
 */
public class SchedulerManager {
	private static final Logger logger = LogManager.getLogger();
	private final SchedulerSettings settings;
	private final Scheduler scheduler;

	/**
	 * Constructs a new scheduler manager instance.
	 * 
	 * @param settings            Settings to use
	 * @param stateManager        State manager
	 * @param transmissionManager Transmission manager
	 * @param clusterManager      Cluster manager
	 * @throws SchedulerException if the scheduler could not be created or started
	 */
	public SchedulerManager(Settings settings, StateManager stateManager, TransmissionManager transmissionManager,
			ClusterManager clusterManager) throws SchedulerException {
		if (settings == null) {
			throw new NullPointerException("Settings must not be null.");
		}

		if (stateManager == null) {
			throw new NullPointerException("State manager must not be null.");
		}

		if (transmissionManager == null) {
			throw new NullPointerException("Transmission manager must not be null.");
		}

		if (clusterManager == null) {
			throw new NullPointerException("Cluster manager must not be null.");
		}

		this.settings = Objects.requireNonNull(settings.getSchedulerSettings());
		this.scheduler = StdSchedulerFactory.getDefaultScheduler();

		scheduler.getContext().put("settings", settings);
		scheduler.getContext().put("stateManager", stateManager);
		scheduler.getContext().put("transmissionManager", transmissionManager);
		scheduler.getContext().put("clusterManager", clusterManager);
		scheduler.start();

		registerTimeTransmissionJob();
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
				.withSchedule(cronSchedule(settings.getTimeTransmissionCron())).build();

		scheduler.scheduleJob(job, trigger);
	}

	private void registerRubricNameTransmissionJob() throws SchedulerException {
		JobDetail job = newJob(RubricNameTransmissionJob.class).withIdentity("rubricNameTransmissionJob", "main")
				.build();
		CronTrigger trigger = newTrigger().withIdentity("rubricNameTransmissionTrigger", "main")
				.withSchedule(cronSchedule(settings.getRubricNameTransmissionCron())).build();

		scheduler.scheduleJob(job, trigger);
	}

	private void registerStateSavingJob() throws SchedulerException {
		JobDetail stateSavingJob = newJob(StateSavingJob.class).withIdentity("stateSavingJob", "main").build();

		CronTrigger stateSavingTrigger = newTrigger().withIdentity("stateSavingTrigger", "main")
				.withSchedule(cronSchedule(settings.getStateSavingCron())).build();

		scheduler.scheduleJob(stateSavingJob, stateSavingTrigger);
	}

	private void registerStateCleaningJob() throws SchedulerException {
		JobDetail job = newJob(StateCleaningJob.class).withIdentity("stateCleaningJob", "main").build();
		CronTrigger trigger = newTrigger().withIdentity("stateCleaningTrigger", "main")
				.withSchedule(cronSchedule(settings.getStateCleaningCron())).build();

		scheduler.scheduleJob(job, trigger);
	}

	private void registerNewsTransmissionJob() throws SchedulerException {
		JobDetail job = newJob(NewsTransmissionJob.class).withIdentity("newsTransmissionJob", "main").build();
		CronTrigger trigger = newTrigger().withIdentity("newsTransmissionTrigger", "main")
				.withSchedule(cronSchedule(settings.getNewsTransmissionCron())).build();

		scheduler.scheduleJob(job, trigger);
	}

	private void registerTransmitterIdentificationJob() throws SchedulerException {
		JobDetail job = newJob(TransmitterIdentificationJob.class).withIdentity("transmitterIdentificationJob", "main")
				.build();
		CronTrigger trigger = newTrigger().withIdentity("transmitterIdentificationTrigger", "main")
				.withSchedule(cronSchedule(settings.getTransmitterIdentificationCron())).build();

		scheduler.scheduleJob(job, trigger);
	}

	/**
	 * Stops the scheduler.
	 */
	public void stop() {
		try {
			scheduler.shutdown();
			logger.info("SchedulerManager successfully stopped");
		} catch (SchedulerException e) {
			logger.error("Failed to stop Scheduler:", e);
		}
	}

}
