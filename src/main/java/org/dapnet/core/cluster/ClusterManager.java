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

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.CoreStartupException;
import org.dapnet.core.DAPNETCore;
import org.dapnet.core.Settings;
import org.dapnet.core.model.NewsList;
import org.dapnet.core.model.Node;
import org.dapnet.core.model.Node.Status;
import org.dapnet.core.model.Rubric;
import org.dapnet.core.model.State;
import org.dapnet.core.model.Transmitter;
import org.dapnet.core.rest.RestListener;
import org.dapnet.core.transmission.TransmissionManager;
import org.dapnet.core.transmission.TransmitterManager;
import org.dapnet.core.transmission.TransmitterManagerListener;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.ExtendedUUID;
import org.jgroups.util.RspList;

public class ClusterManager implements TransmitterManagerListener, RestListener {
	private static final Logger logger = LogManager.getLogger();
	private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	private final JChannel channel;
	private final ChannelListener channelListener;
	private final MembershipListener membershipListener;
	private final ClusterStateListener stateListener;
	private final RpcDispatcher dispatcher;
	private final RequestOptions requestOptions;
	private final TransmissionManager transmissionManager;
	private final TransmitterManager transmitterManager;
	private volatile State state;
	private volatile boolean quorum = true;
	private volatile boolean stopping = false;

	public ClusterManager(TransmissionManager transmissionManager, boolean enforceStartup) throws Exception {
		// Register Transmission
		this.transmissionManager = transmissionManager;
		transmitterManager = transmissionManager.getTransmitterManager();
		transmitterManager.setListener(this);

		// Load State from file
		initState(enforceStartup);

		// Create Channel
		channel = new JChannel(Settings.getClusterSettings().getClusterConfigurationFile());
		channel.setName(readNodeName());
		channel.addAddressGenerator(() -> {
			ExtendedUUID address = ExtendedUUID.randomUUID(channel.getName());
			address.put("version", DAPNETCore.getCoreVersion().getBytes(StandardCharsets.UTF_8));
			return address;
		});

		// Create and register Listener
		channelListener = new org.dapnet.core.cluster.ChannelListener(this);
		channel.addChannelListener(channelListener);

		// Create Dispatcher (for creating Block on top of channel)
		dispatcher = new RpcDispatcher(channel, new RpcListener(this));

		membershipListener = new org.dapnet.core.cluster.MembershipListener(this);
		dispatcher.setMembershipListener(membershipListener);

		stateListener = new org.dapnet.core.cluster.ClusterStateListener(this);
		dispatcher.setStateListener(stateListener);

		// Create default RequestOptions
		requestOptions = new RequestOptions(ResponseMode.GET_ALL, Settings.getClusterSettings().getResponseTimeout());

		try {
			channel.connect(readChannelName());
		} catch (Exception e) {
			logger.fatal("Could not connect to cluster.", e);
			throw new CoreStartupException(e);
		}
	}

	private void initState(boolean enforceStartup) {
		try {
			state = State.readFromFile();
		} catch (FileNotFoundException ex) {
			logger.warn("State file not found.");
		} catch (Exception ex) {
			throw new CoreStartupException(ex);
		}

		if (state == null) {
			state = new State();
			logger.warn("Creating new empty State");
		}

		registerNewsList();
		resetNodeStates();

		// Validate state
		Set<ConstraintViolation<Object>> violations = validator.validate(state);
		if (!violations.isEmpty()) {
			violations.forEach(v -> {
				logger.error("Constraint violation: {} {}", v.getPropertyPath(), v.getMessage());
			});

			if (!enforceStartup) {
				throw new CoreStartupException("State validation failed.");
			} else {
				logger.warn("Startup enforced, ignoring state validation errors.");
			}
		}
	}

	private void registerNewsList() {
		for (Rubric r : state.getRubrics().values()) {
			String rubricName = r.getName().toLowerCase();

			NewsList nl = state.getNews().get(rubricName);
			if (nl == null) {
				nl = new NewsList();
				state.getNews().put(rubricName, nl);
			}

			nl.setHandler(transmissionManager::handleNews);
			nl.setAddHandler(transmissionManager::handleNewsAsCall);
		}
	}

	private void resetNodeStates() {
		for (Node n : state.getNodes().values()) {
			n.setStatus(Status.SUSPENDED);
		}
	}

	public void stop() {
		stopping = true;
		transmitterManager.disconnectFromAll();
	}

	// ### Helper for reading Cluster Config
	// ############################################################################
	private String readChannelName() {
		// Ugly solution but prevents the use of a second configuration file
		String properties = channel.getProperties();
		int gmsPosition = properties.indexOf("pbcast.GMS");
		int namePosition = properties.indexOf("name=", gmsPosition);
		int startPosition = properties.indexOf('@', namePosition) + 1;
		int endPosition = properties.indexOf(';', startPosition);
		return properties.substring(startPosition, endPosition) + DAPNETCore.getCoreVersion();
	}

	private String readNodeName() {
		// Ugly solution but prevents the use of a second configuration file
		String properties = channel.getProperties();
		int gmsPosition = properties.indexOf("pbcast.GMS");
		int namePosition = properties.indexOf("name=", gmsPosition);
		int startPosition = namePosition + 5;
		int endPosition = properties.indexOf('@', startPosition);
		return properties.substring(startPosition, endPosition);
	}

	// ### Quorum
	// #######################################################################################################
	public void checkQuorum() {
		int activeNodeCount = 0; // Count of online and unknown Nodes
		int onlineNodeCount = 0;

		for (Node node : state.getNodes().values()) {
			if (node.getStatus() != Node.Status.SUSPENDED)// Node is in UNKNOWN
															// oder ONLINE state
				activeNodeCount++;
			if (node.getStatus() == Node.Status.ONLINE)
				onlineNodeCount++;
		}

		if (onlineNodeCount == 0) {
			// DAPNETCore is stopping
			return;
		}

		if (onlineNodeCount > (activeNodeCount / 2)) {
			quorum = true;
			logger.info("Cluster has Quorum");
		} else {
			quorum = false;
			logger.warn("Cluster has no Quroum");
		}
	}

	@Override
	public boolean isQuorum() {
		checkQuorum();// Should be unnecessary, but added ensure QuorumCheck
		return quorum;
	}

	// ### Remote Procedure Call
	// ########################################################################################
	@Override
	@SuppressWarnings("rawtypes")
	public boolean handleStateOperation(Collection<Address> destination, String methodName, Object[] args,
			Class[] types) {
		if (!channel.isConnected()) {
			return false;
		}

		try {
			RspList rspList = dispatcher.callRemoteMethods(destination, methodName, args, types, requestOptions);
			if (isRspSuccessful(rspList)) {
				return true;
			} else {
				logger.error("Response: {}", rspList);
			}
		} catch (Exception e) {
			logger.catching(e);
		}

		logger.fatal("Insecure Cluster State");

		return false;
	}

	public boolean updateNodeStatus(Node.Status status) {
		return handleStateOperation(null, "updateNodeStatus", new Object[] { channel.getName(), status },
				new Class[] { String.class, Node.Status.class });
	}

	@SuppressWarnings("rawtypes")
	private boolean isRspSuccessful(RspList list) {
		if (list == null || list.getResults() == null || list.getResults().isEmpty()) {
			return false;
		}

		for (Object result : list.getResults()) {
			try {
				if (result != RpcResponse.OK) {
					return false;
				}
			} catch (Exception e) {
				logger.catching(e);
				return false;
			}
		}

		return true;
	}

	// ### TransmitterDeviceManagerListener
	// #############################################################################
	@Override
	public void handleTransmitterStatusChanged(Transmitter transmitter) {
		transmitter.setNodeName(channel.getName());

		if (state.getTransmitters().containsKey(transmitter.getName())) {
			handleStateOperation(null, "updateTransmitterStatus", new Object[] { transmitter },
					new Class[] { Transmitter.class });
		}
	}

	@Override
	public void handleDisconnectedFromAllTransmitters() {
		if (stopping) {
			updateNodeStatus(Node.Status.SUSPENDED);
			channel.close();
			getState().writeToFile();
		}
	}

	@Override
	public Transmitter handleGetTransmitter(String name) {
		return getState().getTransmitters().get(name);
	}

	// ### Getter and Setter
	// ############################################################################################
	public TransmissionManager getTransmissionManager() {
		return transmissionManager;
	}

	public TransmitterManager getTransmitterManager() {
		return transmitterManager;
	}

	public JChannel getChannel() {
		return channel;
	}

	@Override
	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;

		if (state != null) {
			registerNewsList();
		}
	}
}
