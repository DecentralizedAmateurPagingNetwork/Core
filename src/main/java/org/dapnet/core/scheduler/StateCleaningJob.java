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

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.Settings;
import org.dapnet.core.cluster.ClusterManager;
import org.dapnet.core.model.Call;
import org.dapnet.core.model.State;
import org.dapnet.core.model.Transmitter;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;

public class StateCleaningJob implements Job {
	private static final Logger logger = LogManager.getLogger();

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {

		SchedulerContext schedulerContext = null;
		try {
			schedulerContext = context.getScheduler().getContext();
			ClusterManager clusterManager = (ClusterManager) schedulerContext.get("clusterManager");

			State state = clusterManager.getState();
			Instant now = Instant.now();

			cleanCalls(state, now);
			cleanNews(state, now);
			cleanTransmitters(state, now, clusterManager);

			state.writeToFile();
		} catch (SchedulerException e) {
			logger.error("Failed to execute StateCleaningJob", e);
		}
	}

	private static void cleanCalls(State state, Instant now) {
		Duration exp = Duration.ofMinutes(Settings.getModelSettings().getCallExpirationTimeInMinutes());
		Iterator<Call> it = state.getCalls().iterator();
		while (it.hasNext()) {
			Call call = it.next();
			if (now.isAfter(call.getTimestamp().plus(exp))) {
				it.remove();
			}
		}
	}

	private static void cleanNews(State state, Instant now) {
		Duration ttl = Duration.ofMinutes(Settings.getModelSettings().getNewsExpirationTimeInMinutes());
		state.getNews().values().forEach(nl -> nl.removeExpired(now, ttl));
	}

	private static void cleanTransmitters(State state, Instant now, ClusterManager manager) {
		Duration exp = Duration.ofDays(Settings.getModelSettings().getTransmitterExpirationDays());

		// Clean only own transmitters
		ConcurrentMap<String, Transmitter> transmitters = state.getTransmitters();
		Set<String> names = manager.getNodeTransmitterNames();
		for (String name : names) {
			Transmitter t = transmitters.get(name);
			if (t != null && t.getLastConnected() != null && now.isAfter(t.getLastConnected().plus(exp))) {
				logger.info("Removing transmitter due to inactivity: {}", name);
				transmitters.remove(name);
				removeTransmitter(manager, name);
			}
		}
	}

	private static void removeTransmitter(ClusterManager manager, String name) {
		try {
			manager.handleStateOperation(null, "deleteTransmitter", new Object[] { name },
					new Class[] { String.class });
		} catch (Throwable t) {
			logger.error("Failed to remove transmitter from cluster.", t);
		}
	}
}
