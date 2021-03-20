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

package org.dapnet.core.cluster;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.Program;
import org.dapnet.core.model.StateManager;
import org.jgroups.Message;

import jakarta.validation.ConstraintViolation;

public class MessageListener implements org.jgroups.MessageListener {
	private static final Logger logger = LogManager.getLogger();
	private final ClusterManager clusterManager;
	private final StateManager stateManager;

	public MessageListener(ClusterManager clusterManager) {
		this.clusterManager = Objects.requireNonNull(clusterManager, "Cluster manager must not be null.");
		stateManager = Objects.requireNonNull(clusterManager.getStateManager(), "State manager must not be null.");
	}

	@Override
	public void receive(Message message) {
	}

	@Override
	public void getState(OutputStream outputStream) throws Exception {
		logger.info("Start sending State to other Node");

		stateManager.writeStateToStream(outputStream);

		logger.info("Finished sending State to other Node");
	}

	@Override
	public void setState(InputStream inputStream) throws Exception {
		logger.info("Receiving State from other Node");

		Set<ConstraintViolation<Object>> violations = stateManager.loadStateFromStream(inputStream, false);
		if (violations.isEmpty()) {
			stateManager.writeStateToFile();

			logger.info("State successfully received");
		} else {
			violations.forEach(v -> {
				logger.error("Constraint violation: {} {}", v.getPropertyPath(), v.getMessage());
			});

			logger.fatal("Discarding received state");
			// FIXME Does this work?
			Program.shutdown();
		}

		// Check if node name is in received state.
		String nodeName = clusterManager.getChannel().getName();

		Lock lock = stateManager.getLock().readLock();
		lock.lock();

		try {
			if (!stateManager.getNodes().containsKey(nodeName)) {
				logger.fatal("Current node name does not exist in received state: {}", nodeName);
				Program.shutdown();
			}
		} finally {
			lock.unlock();
		}
	}
}
