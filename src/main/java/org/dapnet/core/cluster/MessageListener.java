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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.DAPNETCore;
import org.dapnet.core.model.State;
import org.jgroups.Message;
import org.jgroups.util.Util;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

public class MessageListener implements org.jgroups.MessageListener {
	private static final Logger logger = LogManager.getLogger();
	private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
	private final ClusterManager clusterManager;

	public MessageListener(ClusterManager clusterManager) {
		this.clusterManager = clusterManager;
	}

	@Override
	public void receive(Message message) {
		// Not used, only using Messages forwarded to RpcListener
	}

	@Override
	public void getState(OutputStream outputStream) throws Exception {
		logger.info("Start sending State to other Node");
		synchronized (clusterManager.getState()) {
			Util.objectToStream(clusterManager.getState(), new DataOutputStream(outputStream));
		}
		logger.info("Finished sending State to other Node");
	}

	@Override
	public void setState(InputStream inputStream) throws Exception {
		logger.info("Receiving State from other Node");
		State state = (State) Util.objectFromStream(new DataInputStream(inputStream));

		synchronized (clusterManager.getState()) {
			clusterManager.setState(state);
			clusterManager.getState().setModelReferences();
		}

		Set<ConstraintViolation<Object>> constraintViolations = validator.validate(state);
		for (ConstraintViolation<Object> violation : constraintViolations) {
			logger.error("Error validating received State: {} {}", violation.getPropertyPath(), violation.getMessage());
		}
		if (constraintViolations.size() != 0) {
			logger.fatal("Discarding received State");
			// FIXME Does this work?
			DAPNETCore.shutdown();
		}

		clusterManager.getState().writeToFile();
		logger.info("State successfully received");
	}
}
