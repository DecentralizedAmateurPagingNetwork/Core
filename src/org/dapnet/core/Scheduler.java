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

package org.dapnet.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.cluster.ClusterManager;
import org.dapnet.core.model.Rubric;
import org.dapnet.core.transmission.TransmissionManager;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Scheduler {
    private static final Logger logger = LogManager.getLogger(Scheduler.class.getName());
    private TransmissionManager transmissionManager;
    private ClusterManager clusterManager;

    public Scheduler(TransmissionManager transmissionManager, ClusterManager clusterManager) {
        this.transmissionManager = transmissionManager;
        this.clusterManager = clusterManager;

        startTimeTransmissionTimer();
        startRubricTransmissionTimer();
        logger.info("Scheduler successfully started");
    }

    public void stop()
    {
        rubricTransmissionTimer.cancel();
        timeTransmissionTimer.cancel();
        logger.info("Scheduler successfully stopped");
    }

    //TimeTransmission
    private Timer timeTransmissionTimer;
    final TimerTask timeTransmissionTask = new TimerTask() {
        public void run() {
            //Possibility to implement TimeZones by adding here a TransmitterGroup
            transmissionManager.handleTime(new Date());
        }
    };

    private void startTimeTransmissionTimer()
    {
        timeTransmissionTimer = new Timer();
        int seconds = Calendar.getInstance().get(Calendar.SECOND);
        //Make sure Timer fires every 20 Minutes with 0<SEC<30
        timeTransmissionTimer.schedule(
                timeTransmissionTask,
                (60 + 15 - seconds) * 1000,
                Settings.getSchedulerSettings().getTimeTransmissionPeriod());
    }

    private void stopTimeTransmissionTimer()
    {
        timeTransmissionTimer.cancel();
    }

    //RubricTransmission
    private Timer rubricTransmissionTimer;
    final TimerTask rubricTransmissionTask = new TimerTask() {
        public void run() {
            for(Rubric rubric : clusterManager.getState().getRubrics())
                transmissionManager.handleRubric(rubric);
        }
    };

    private void startRubricTransmissionTimer()
    {
        rubricTransmissionTimer = new Timer();
        rubricTransmissionTimer.schedule(
                rubricTransmissionTask,
                120 * 1000,
                Settings.getSchedulerSettings().getRubricTransmissionTimer());
    }

    private void stopRubricTransmissionTimer()
    {
        rubricTransmissionTimer.cancel();
    }

}
