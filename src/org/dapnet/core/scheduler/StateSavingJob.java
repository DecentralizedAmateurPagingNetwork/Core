/*
 * DAPNET CORE PROJECT
 * Copyright (C) 2016
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
import org.dapnet.core.cluster.ClusterManager;
import org.quartz.*;

public class StateSavingJob implements Job
{
    private static final Logger logger = LogManager.getLogger(StateSavingJob.class.getName());

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SchedulerContext schedulerContext = null;
        try {
            schedulerContext = context.getScheduler().getContext();
            ClusterManager clusterManager = (ClusterManager) schedulerContext.get("clusterManager");

            clusterManager.getState().writeToFile();
        } catch (SchedulerException e) {
            logger.fatal("Failed to execute StateSavingJob", e);
        }
    }
}

