package org.dapnet.core.scheduler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.cluster.ClusterManager;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerContext;

public class NewsTransmissionJob implements Job {
	private static final Logger LOGGER = LogManager.getLogger();

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			SchedulerContext schedulerContext = context.getScheduler().getContext();
			ClusterManager clusterManager = (ClusterManager) schedulerContext.get("clusterManager");

			try {
				clusterManager.getState().getNews().values().forEach(nl -> nl.triggerAll());
			} catch (Throwable t) {
				LOGGER.fatal("Failed to send news.", t);
			}
		} catch (Throwable t) {
			LOGGER.fatal("Failed to execute news transmission job.", t);
		}
	}

}
