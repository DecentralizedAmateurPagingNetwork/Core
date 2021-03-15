package org.dapnet.core.scheduler;

import java.util.concurrent.locks.Lock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.model.StateManager;
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
			StateManager stateManager = (StateManager) schedulerContext.get("stateManager");

			Lock lock = stateManager.getLock().readLock();
			lock.lock();

			try {
				stateManager.getRepository().getNews().values().forEach(nl -> {
					try {
						nl.triggerAll();
					} catch (Throwable t) {
						LOGGER.fatal("Failed to send news.", t);
					}
				});
			} finally {
				lock.unlock();
			}
		} catch (Throwable t) {
			LOGGER.fatal("Failed to execute news transmission job.", t);
		}
	}

}
