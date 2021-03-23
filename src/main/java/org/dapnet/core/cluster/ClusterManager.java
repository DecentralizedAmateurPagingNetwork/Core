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
import java.util.Objects;
import java.util.concurrent.locks.Lock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.CoreStartupException;
import org.dapnet.core.Program;
import org.dapnet.core.model.ModelRepository;
import org.dapnet.core.model.NewsList;
import org.dapnet.core.model.Node;
import org.dapnet.core.model.Node.Status;
import org.dapnet.core.model.Rubric;
import org.dapnet.core.model.StateManager;
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

/**
 * The cluster manager is responsible for the DAPNET cluster connection.
 * 
 * @author Philipp Thiel
 */
public final class ClusterManager implements TransmitterManagerListener, RestListener {
	private static final Logger logger = LogManager.getLogger();

	private final StateManager stateManager;
	private final JChannel channel;
	private final ChannelListener channelListener;
	private final MembershipListener membershipListener;
	private final MessageListener messageListener;
	private final RpcDispatcher dispatcher;
	private final RequestOptions requestOptions;
	private final TransmissionManager transmissionManager;
	private final TransmitterManager transmitterManager;
	private volatile boolean quorum = true;
	private volatile boolean stopping = false;

	/**
	 * Constructs a new cluster manager instance.
	 * 
	 * @param stateManager        State manager
	 * @param transmissionManager Transmission manager
	 * @throws Exception if cluster construction failed
	 */
	public ClusterManager(StateManager stateManager, TransmissionManager transmissionManager) throws Exception {
		this.stateManager = Objects.requireNonNull(stateManager, "State manager must not be null.");

		// Register Transmission
		this.transmissionManager = Objects.requireNonNull(transmissionManager,
				"Transmission manager must not be null.");
		transmitterManager = transmissionManager.getTransmitterManager();
		transmitterManager.setListener(this);

		// Perform additional state initialization
		initState();

		final ClusterSettings settings = transmitterManager.getSettings().getClusterSettings();

		// Create Channel
		channel = new JChannel(settings.getClusterConfigurationFile());
		channel.setName(readNodeName());
		channel.addAddressGenerator(() -> {
			ExtendedUUID address = ExtendedUUID.randomUUID(channel.getName());
			address.put("version", Program.getCoreVersion().getBytes(StandardCharsets.UTF_8));
			return address;
		});

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
		requestOptions = new RequestOptions(ResponseMode.GET_ALL, settings.getResponseTimeout());

		try {
			channel.connect(readChannelName());
		} catch (Exception e) {
			logger.fatal("Could not connect to cluster.", e);
			throw new CoreStartupException(e);
		}
	}

	private void initState() {
		Lock lock = stateManager.getLock().writeLock();
		lock.lock();

		try {
			registerNewsList();
			resetNodeStates();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Gets the state manager.
	 * 
	 * @return State manager
	 */
	public StateManager getStateManager() {
		return stateManager;
	}

	private void registerNewsList() {
		ModelRepository<Rubric> rubrics = stateManager.getRubrics();
		ModelRepository<NewsList> news = stateManager.getNews();

		for (Rubric r : rubrics.values()) {
			NewsList nl = news.get(r.getName());
			if (nl == null) {
				nl = new NewsList();
				news.put(r.getName(), nl);
			}

			nl.setHandler(transmissionManager::sendNewsAsRubric);
			nl.setAddHandler(transmissionManager::sendNewsAsCall);
		}
	}

	private void resetNodeStates() {
		ModelRepository<Node> nodes = stateManager.getNodes();
		for (Node n : nodes.values()) {
			n.setStatus(Status.SUSPENDED);
		}
	}

	/**
	 * Stops the cluster manager.
	 */
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
		return properties.substring(startPosition, endPosition) + Program.getCoreVersion();
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
		// Count of online and unknown Nodes
		int activeNodeCount = 0;
		int onlineNodeCount = 0;

		Lock lock = stateManager.getLock().readLock();
		lock.lock();

		try {
			ModelRepository<Node> nodes = stateManager.getNodes();
			for (Node node : nodes.values()) {
				if (node.getStatus() != Node.Status.SUSPENDED) {
					// Node is in UNKNOWN
					// oder ONLINE state
					activeNodeCount++;
				}

				if (node.getStatus() == Node.Status.ONLINE) {
					onlineNodeCount++;
				}
			}
		} finally {
			lock.unlock();
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
			if (isSuccessful(rspList)) {
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
	private boolean isSuccessful(RspList list) {
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
		Lock lock = stateManager.getLock().writeLock();
		lock.lock();

		try {
			transmitter.setNodeName(channel.getName());
		} finally {
			lock.unlock();
		}

		// XXX I'm not sure if holding the lock could lead to a deadlock, that's why it
		// is a bit awkward looking here...
		boolean exists = false;

		lock = stateManager.getLock().readLock();
		lock.lock();

		try {
			ModelRepository<Transmitter> transmitters = stateManager.getTransmitters();
			exists = transmitters.containsKey(transmitter.getName());
		} finally {
			lock.unlock();
		}

		if (exists) {
			handleStateOperation(null, "updateTransmitterStatus", new Object[] { transmitter },
					new Class[] { Transmitter.class });
		}
	}

	@Override
	public void handleDisconnectFromAllTransmitters() {
		if (stopping) {
			Lock lock = stateManager.getLock().writeLock();
			lock.lock();

			try {
				updateNodeStatus(Node.Status.SUSPENDED);
			} finally {
				lock.unlock();
			}

			channel.close();

			try {
				stateManager.writeStateToFile();
			} catch (FileNotFoundException ex) {
				logger.fatal("Failed to write state file: {}", ex.getMessage());
			} catch (Exception ex) {
				logger.fatal("Failed to write state file.", ex);
			}
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
}
