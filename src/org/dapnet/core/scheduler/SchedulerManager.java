/*
 * DAPNET CORE PROJECT
 * Copyright (C) 2015
 *
 * Daniel Sialkowski
 *
 * daniel.sialkowski@rwth-aachen.de
 *
 * Institut für Hochfrequenztechnik
 * RWTH AACHEN UNIVERSITY
 * Melatener Str. 25
 * 52074 Aachen
 */

package org.dapnet.core.scheduler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.Settings;
import org.dapnet.core.cluster.ClusterManager;
import org.dapnet.core.transmission.TransmissionManager;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

public class SchedulerManager {
    private static final Logger logger = LogManager.getLogger(SchedulerManager.class.getName());
    private Scheduler scheduler;

    public SchedulerManager(TransmissionManager transmissionManager, ClusterManager clusterManager) throws SchedulerException {
        this.scheduler = StdSchedulerFactory.getDefaultScheduler();

        scheduler.getContext().put("transmissionManager", transmissionManager);
        scheduler.getContext().put("clusterManager", clusterManager);
        scheduler.start();

        registerTimeTransmissionJob();
        registerRubricNameTransmissionJob();

        logger.info("SchedulerManager successfully started");
    }

    private void registerTimeTransmissionJob() throws SchedulerException {
        JobDetail timeTransmissionJob = newJob(TimeTransmissionJob.class)
                .withIdentity("timeTransmissionJob", "main")
                .build();

        CronTrigger timeTransmissionTrigger = newTrigger()
                .withIdentity("timeTransmissionTrigger", "main")
                .withSchedule(cronSchedule(Settings.getSchedulerSettings().getTimeTransmissionCron()))
                .build();
        scheduler.scheduleJob(timeTransmissionJob, timeTransmissionTrigger);
    }

    private void registerRubricNameTransmissionJob() throws SchedulerException {
        JobDetail rubricNameTransmissionJob = newJob(RubricNameTransmissionJob.class)
                .withIdentity("rubricNameTransmissionJob", "main")
                .build();

        CronTrigger rubricNameTransmissionTrigger = newTrigger()
                .withIdentity("rubricNameTransmissionTrigger", "main")
                .withSchedule(cronSchedule(Settings.getSchedulerSettings().getRubricNameTransmissionCron()))
                .build();
        scheduler.scheduleJob(rubricNameTransmissionJob, rubricNameTransmissionTrigger);
    }

    public void stop() {
        try {
            scheduler.shutdown();
        } catch (SchedulerException e) {
            logger.error("Failed to stop Scheduler:", e);
        }
        logger.info("SchedulerManager successfully stopped");
    }


}
