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
import org.dapnet.core.transmission.TransmissionManager;
import org.quartz.*;

public class NewsTransmissionJob implements Job{
    private static final Logger logger = LogManager.getLogger(SchedulerManager.class.getName());

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.info("Executing Job");
        SchedulerContext schedulerContext = null;
        try {
            schedulerContext = context.getScheduler().getContext();
            TransmissionManager transmissionManager = (TransmissionManager) schedulerContext.get("transmissionManager");

            System.out.println("Dummy News Transmit");
            //transmissionManager.handleNews(context.getJobDetail().getKey());

        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}
