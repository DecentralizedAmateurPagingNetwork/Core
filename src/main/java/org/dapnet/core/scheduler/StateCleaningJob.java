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

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.Settings;
import org.dapnet.core.model.Call;
import org.dapnet.core.model.Repository;
import org.dapnet.core.model.StateManager;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;

public class StateCleaningJob implements Job {
	private static final Logger logger = LogManager.getLogger();

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			SchedulerContext schedulerContext = context.getScheduler().getContext();
			StateManager stateManager = (StateManager) schedulerContext.get("stateManager");

			Lock lock = stateManager.getLock().writeLock();
			lock.lock();

			try {
				Instant now = Instant.now();

				cleanCalls(stateManager, now);
				cleanNews(stateManager, now);
				// FIXME This is broken
				// cleanTransmitters(clusterManager, now);
			} finally {
				lock.unlock();
			}

			stateManager.writeStateToFile(Settings.getModelSettings().getStateFile());
		} catch (SchedulerException e) {
			logger.error("Failed to execute StateCleaningJob", e);
		} catch (IOException e) {
			logger.fatal("Failed to write the state file", e);
		}
	}

	private static void cleanCalls(Repository repo, Instant now) {
		Duration exp = Duration.ofMinutes(Settings.getModelSettings().getCallExpirationTimeInMinutes());

		Iterator<Call> it = repo.getCalls().iterator();
		while (it.hasNext()) {
			Call call = it.next();
			if (now.isAfter(call.getTimestamp().plus(exp))) {
				it.remove();
			}
		}
	}

	private static void cleanNews(Repository repo, Instant now) {
		Duration ttl = Duration.ofMinutes(Settings.getModelSettings().getNewsExpirationTimeInMinutes());
		repo.getNews().values().forEach(nl -> nl.removeExpired(now, ttl));
	}

//	private static void cleanTransmitters(ClusterManager manager, Instant now) {
//		Duration exp = Duration.ofDays(Settings.getModelSettings().getTransmitterExpirationDays());
//
//		Collection<Transmitter> transmitters = new ArrayList<>(manager.getState().getTransmitters().values());
//		for (Transmitter t : transmitters) {
//			if (t != null && t.getLastConnected() != null && t.getStatus() != Transmitter.Status.ONLINE
//					&& now.isAfter(t.getLastConnected().plus(exp))) {
//				logger.info("Removing transmitter due to inactivity: {}", t.getName());
//				deleteTransmitter(manager, t.getName().toLowerCase());
//			}
//		}
//	}
//
//	private static void deleteTransmitter(ClusterManager manager, String name) {
//		manager.handleStateOperation(null, "deleteTransmitter", new Object[] { name }, new Class[] { String.class });
//	}

}
