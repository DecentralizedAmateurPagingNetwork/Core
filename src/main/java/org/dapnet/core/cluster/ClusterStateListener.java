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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.DAPNETCore;
import org.dapnet.core.model.State;
import org.jgroups.util.Util;

public class ClusterStateListener implements org.jgroups.StateListener {
	private static final Logger logger = LogManager.getLogger();
	private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
	private final ClusterManager clusterManager;

	public ClusterStateListener(ClusterManager clusterManager) {
		this.clusterManager = clusterManager;
	}

	@Override
	public void getState(OutputStream outputStream) throws Exception {
		logger.info("Start sending State to other Node");

		State state = clusterManager.getState();
		Util.objectToStream(state, new DataOutputStream(outputStream));

		logger.info("Finished sending State to other Node");
	}

	@Override
	public void setState(InputStream inputStream) throws Exception {
		logger.info("Receiving State from other Node");

		State state = (State) Util.objectFromStream(new DataInputStream(inputStream));
		clusterManager.setState(state);
		state.setModelReferences();

		// Validate state
		Set<ConstraintViolation<Object>> violations = validator.validate(state);
		if (violations.isEmpty()) {
			state.writeToFile();

			logger.info("State successfully received");
		} else {
			violations.forEach(v -> {
				logger.error("Constraint violation: {} {}", v.getPropertyPath(), v.getMessage());
			});

			logger.fatal("Discarding received State");
			// FIXME Does this work?
			DAPNETCore.shutdown();
		}

		// Check if node name is in received state.
		String nodeName = clusterManager.getChannel().getName();
		if (!state.getNodes().containsKey(nodeName)) {
			logger.fatal("Current node name does not exist in received state: {}", nodeName);
			DAPNETCore.shutdown();
		}
	}
}
