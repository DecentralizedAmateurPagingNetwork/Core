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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.cluster.ClusterManager;
import org.dapnet.core.model.Rubric;
import org.dapnet.core.transmission.TransmissionManager;
import org.quartz.*;

public class RubricNameTransmissionJob implements Job {
    private static final Logger logger = LogManager.getLogger(RubricNameTransmissionJob.class.getName());

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SchedulerContext schedulerContext = null;
        try {
            schedulerContext = context.getScheduler().getContext();
            TransmissionManager transmissionManager = (TransmissionManager) schedulerContext.get("transmissionManager");
            ClusterManager clusterManager = (ClusterManager) schedulerContext.get("clusterManager");

            for (Rubric rubric : clusterManager.getState().getRubrics())
                transmissionManager.handleRubric(rubric);
        } catch (SchedulerException e) {
            logger.error("Failed to execute RubricNameTransmissionJob", e);
        }
    }
}
