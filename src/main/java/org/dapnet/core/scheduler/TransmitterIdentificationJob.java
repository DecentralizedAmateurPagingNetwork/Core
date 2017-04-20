package org.dapnet.core.scheduler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.cluster.ClusterManager;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerContext;

public class TransmitterIdentificationJob implements Job {
	private static final Logger LOGGER = LogManager.getLogger();

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			SchedulerContext schedulerContext = context.getScheduler().getContext();
			ClusterManager clusterManager = (ClusterManager) schedulerContext.get("clusterManager");

			clusterManager.getTransmissionManager().handleIdentification();
		} catch (Throwable cause) {
			LOGGER.fatal("Failed to execute transmitter identification job.", cause);
		}
	}
}
