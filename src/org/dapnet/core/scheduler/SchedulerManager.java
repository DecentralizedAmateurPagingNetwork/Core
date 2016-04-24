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
import org.dapnet.core.model.News;
import org.dapnet.core.transmission.TransmissionManager;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class SchedulerManager {
    private static final Logger logger = LogManager.getLogger(SchedulerManager.class.getName());
    private Scheduler scheduler;

    public SchedulerManager() throws SchedulerException {
        this.scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        logger.info("SchedulerManager successfully started");
    }

    //todo Überprüfen, dass nicht mehrfach aufgerufen
    public void registerTimeTransmissionJob(TransmissionManager transmissionManager) throws SchedulerException {
        scheduler.getContext().put("transmissionManager", transmissionManager);

        JobDetail timeTransmissionJob = newJob(TimeTransmissionJob.class)
                .withIdentity("timeTransmissionJob", "main")
                .build();

        CronTrigger timeTransmissionTrigger = newTrigger()
                .withIdentity("timeTransmissionTrigger", "main")
                .withSchedule(cronSchedule(Settings.getSchedulerSettings().getTimeTransmissionCron()))
                .build();
        scheduler.scheduleJob(timeTransmissionJob, timeTransmissionTrigger);
        logger.info("Started TimeTransmissionJob");
    }

    //todo Überprüfen, dass nicht mehrfach aufgerufen
    public void registerRubricNameTransmissionJob(
            TransmissionManager transmissionManager, ClusterManager clusterManager) throws SchedulerException {
        scheduler.getContext().put("transmissionManager", transmissionManager);
        scheduler.getContext().put("clusterManager", clusterManager);

        JobDetail rubricNameTransmissionJob = newJob(RubricNameTransmissionJob.class)
                .withIdentity("rubricNameTransmissionJob", "main")
                .build();

        CronTrigger rubricNameTransmissionTrigger = newTrigger()
                .withIdentity("rubricNameTransmissionTrigger", "main")
                .withSchedule(cronSchedule(Settings.getSchedulerSettings().getRubricNameTransmissionCron()))
                .build();
        scheduler.scheduleJob(rubricNameTransmissionJob, rubricNameTransmissionTrigger);
        logger.info("Started RubricNameTransmissionJob");
    }

    //todo muss auch beim start erfolgen
    public void registerNewsTransmissionJob(
            TransmissionManager transmissionManager, News news) throws SchedulerException {
        scheduler.getContext().put("transmissionManager", transmissionManager);

        JobDetail newsTransmissionJob = newJob(NewsTransmissionJob.class)
                .withIdentity(news.getUuid().toString(), "rubric")
                .build();
        scheduler.addJob(newsTransmissionJob, true, true);

        TriggerBuilder triggerBuilder= newTrigger()
                .forJob(newsTransmissionJob);

        //Start
        if(news.getValidity()==null || news.getValidity().getStart()==null)
            triggerBuilder.startNow();
        else
            triggerBuilder.startAt(news.getValidity().getStart());

        //End
        if(news.getValidity()!=null && news.getValidity().getEnd()!=null)
            triggerBuilder.endAt(news.getValidity().getEnd());

        //Cron Trigger
        if(news.getScheduling()!=null && news.getScheduling().getCron()!=null) {
            Trigger cronTrigger = triggerBuilder
                    .withIdentity(news.getUuid().toString(), "rubric_cron")
                    .withSchedule(cronSchedule(news.getScheduling().getCron())).build();
            scheduler.scheduleJob(cronTrigger);
            logger.info("Started NewsTransmissionJob: " + cronTrigger.getKey() + " - " + cronTrigger.getJobKey());
        }
        //Simple Trigger
        if(news.getScheduling()!=null && news.getScheduling().getRepeatIntervallInMinutes()>0)
        {
            Trigger simpleTrigger = triggerBuilder
                    .withIdentity(news.getUuid().toString(), "rubric_simple")
                    .withSchedule(simpleSchedule()
                    .withIntervalInMinutes(news.getScheduling().getRepeatIntervallInMinutes())
                    .repeatForever())
                    .build();
            scheduler.scheduleJob(simpleTrigger);
            logger.info("Started NewsTransmissionJob: " + simpleTrigger.getKey() + " - " + simpleTrigger.getJobKey());
        }
        //No Scheduling
        if(news.getScheduling()==null
                ||(news.getScheduling().getCron()==null && news.getScheduling().getRepeatIntervallInMinutes()<=0))
        {
            Trigger simpleTrigger = triggerBuilder
                    .withIdentity(news.getUuid().toString(), "rubric_no_scheduling")
                    .build();
            scheduler.scheduleJob(simpleTrigger);
            logger.info("Started NewsTransmissionJob: " + simpleTrigger.getKey() + " - " + simpleTrigger.getJobKey());
        }
    }

    public void stop() {
        try {
            scheduler.shutdown();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        logger.info("SchedulerManager successfully stopped");
    }
}
