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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.DAPNETCore;
import org.dapnet.core.Settings;
import org.dapnet.core.model.Node;
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
import org.jgroups.protocols.AUTH;
import org.jgroups.util.RspList;

public class ClusterManager implements TransmitterManagerListener, RestListener {
	private static final Logger logger = LogManager.getLogger();
	private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	private JChannel channel;
	private ChannelListener channelListener;
	private MembershipListener membershipListener;
	private MessageListener messageListener;
	private RpcDispatcher dispatcher;
	private RequestOptions requestOptions;

	private State state;

	private TransmissionManager transmissionManager;
	private TransmitterManager transmitterManager;

	private boolean quorum = true;
	private boolean stopping = false;

	public ClusterManager(TransmissionManager transmissionManager) throws Exception {
		// Register Transmission
		this.transmissionManager = transmissionManager;
		this.transmitterManager = transmissionManager.getTransmitterManager();
		this.transmitterManager.setListener(this);

		// Initiate State
		initState();

		// Set Reference for Authentication System
		ClusterAuthentication.setClusterManger(this);

		// Create Channel
		channel = new JChannel(Settings.getClusterSettings().getClusterConfigurationFile());
		channel.setName(getNodeName());

		// Create Dispatcher (for creating Block on top of channel)
		dispatcher = new RpcDispatcher(channel, new RpcListener(this));

		// Create and register Listener
		channelListener = new org.dapnet.core.cluster.ChannelListener(this);
		dispatcher.addChannelListener(channelListener);

		membershipListener = new org.dapnet.core.cluster.MembershipListener(this);
		dispatcher.setMembershipListener(membershipListener);

		messageListener = new org.dapnet.core.cluster.MessageListener(this);
		dispatcher.setMessageListener(messageListener);

		// Create default RequestOptions
		requestOptions = new RequestOptions(ResponseMode.GET_ALL, Settings.getClusterSettings().getResponseTimeout());

		// Connect to channel
		try {
			channel.connect(getChannelName());
		} catch (Exception e) {
			logger.fatal("Could not connect to cluster");
			System.out.println("Could not connect to cluster. Check your settings and make sure your node is already "
					+ "registered.");
			DAPNETCore.stopDAPNETCore();
		}

		// Register transmitters
		transmitterManager.addTransmitters(getNodeTransmitter());
	}

	private void initState() {
		// Create
		try {
			// Read State from File (will be overwritten if joining an existing
			// Cluster)
			state = State.readFromFile();
		} catch (Exception e) {
		}
		if (state == null) {
			state = new State();
			logger.warn("Creating new empty State");
		}

		// Validate
		Set<ConstraintViolation<Object>> constraintViolations = validator.validate(state);
		for (ConstraintViolation<Object> violation : constraintViolations) {
			logger.error("Error validating State.json: " + violation.getPropertyPath() + " " + violation.getMessage());
		}

		if (constraintViolations.size() != 0) {
			DAPNETCore.stopDAPNETCore();
		}
	}

	public List<Transmitter> getNodeTransmitter() {
		ArrayList<Transmitter> myTransmitters = new ArrayList<>();
		for (Transmitter transmitter : state.getTransmitters().values()) {
			if (transmitter.getNodeName().equals(channel.getName())) {
				myTransmitters.add(transmitter);
			}
		}

		return myTransmitters;
	}

	public void stop() {
		stopping = true;
		transmitterManager.disconnectFromAll();
	}

	// ### Helper for reading Cluster Config
	// ############################################################################
	private String getChannelName() {
		// Ugly solution but prevents the use of a second configuration file
		String properties = channel.getProperties();
		int gmsPosition = properties.indexOf("pbcast.GMS");
		int namePosition = properties.indexOf("name=", gmsPosition);
		int startPosition = properties.indexOf('@', namePosition) + 1;
		int endPosition = properties.indexOf(';', startPosition);
		return properties.substring(startPosition, endPosition) + DAPNETCore.getCoreVersion();
	}

	private String getNodeName() {
		// Ugly solution but prevents the use of a second configuration file
		String properties = channel.getProperties();
		int gmsPosition = properties.indexOf("pbcast.GMS");
		int namePosition = properties.indexOf("name=", gmsPosition);
		int startPosition = namePosition + 5;
		int endPosition = properties.indexOf('@', startPosition);
		return properties.substring(startPosition, endPosition);
	}

	String getAuthValue() {
		return ((ClusterAuthentication) ((AUTH) channel.getProtocolStack().findProtocol("AUTH")).getAuthToken())
				.getAuthValue();
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

	public boolean isQuorum() {
		checkQuorum();// Should be unnecessary, but added ensure QuorumCheck
		return quorum;
	}

	// ### Remote Procedure Call
	// ########################################################################################
	@SuppressWarnings("rawtypes")
	public boolean handleStateOperation(Collection<Address> destination, String methodName, Object[] args,
			Class[] types) {
		try {
			RspList rspList = dispatcher.callRemoteMethods(destination, methodName, args, types, requestOptions);
			if (isRspSuccessful(rspList)) {
				return true;
			} else {
				logger.error("Response: " + rspList.toString());
			}
		} catch (Exception e) {
			logger.catching(e);
		}
		logger.fatal("Insecure Cluster State");
		// TODO Rollback
		return false;
	}

	public boolean updateNodeStatus(Node.Status status) {
		return handleStateOperation(null, "updateNodeStatus", new Object[] { channel.getName(), status },
				new Class[] { String.class, Node.Status.class });

	}

	@SuppressWarnings("rawtypes")
	private boolean isRspSuccessful(RspList list) {
		if (list == null || list.getResults() == null || list.getResults().isEmpty())
			return false;
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
		String name = transmitter.getName();
		if (state.getTransmitters().containsKey(name)) {
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

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}
}
