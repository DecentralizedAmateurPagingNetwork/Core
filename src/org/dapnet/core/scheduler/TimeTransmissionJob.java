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

import org.dapnet.core.transmission.TransmissionManager;
import org.quartz.*;

import java.util.Date;

public class TimeTransmissionJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        SchedulerContext schedulerContext = null;
        try {
            schedulerContext = context.getScheduler().getContext();
            TransmissionManager transmissionManager = (TransmissionManager) schedulerContext.get("transmissionManager");

            //Possibility to implement TimeZones by adding here a TransmitterGroup
            transmissionManager.handleTime(new Date());
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}
