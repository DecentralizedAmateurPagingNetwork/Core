package org.dapnet.core.transmission;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.model.NamedObject;
import org.dapnet.core.model.StateManager;
import org.dapnet.core.model.Transmitter;
import org.dapnet.core.model.Transmitter.Status;
import org.dapnet.core.model.TransmitterGroup;

/**
 * This class manages connected transmitters.
 * 
 * @author Philipp Thiel
 */
public class TransmitterManager {
	private static final Logger logger = LogManager.getLogger();
	private final ConcurrentMap<String, TransmitterClient> connectedClients = new ConcurrentHashMap<>();
	private final StateManager stateManager;
	private volatile TransmitterManagerListener listener;

	public TransmitterManager(StateManager stateManager) {
		this.stateManager = Objects.requireNonNull(stateManager, "State manager must not be null.");
	}

	/**
	 * Gets the state manager instance.
	 * 
	 * @return State manager
	 */
	public StateManager getStateManager() {
		return stateManager;
	}

	/**
	 * Sets the event listener.
	 * 
	 * @param listener Event listener instance.
	 */
	public void setListener(TransmitterManagerListener listener) {
		// The listener can be null
		this.listener = listener;
	}

	/**
	 * Sends a message to all connected transmitters.
	 * 
	 * @param message Message to send.
	 */
	public void sendMessage(PagerMessage message) {
		connectedClients.values().forEach(c -> c.sendMessage(message));
	}

	/**
	 * Sends a message to one or more transmitter groups.
	 * 
	 * @param message Message to send.
	 * @param groups  Transmitter groups to send to.
	 */
	public void sendMessage(PagerMessage message, Collection<TransmitterGroup> groups) {
		getTransmitterNames(groups).forEach(n -> sendMessage(message, n));
	}

	/**
	 * Sends multiple messages to one or more transmitter groups.
	 * 
	 * @param messages Messages to send.
	 * @param groups   Transmitter groups to send to.
	 */
	public void sendMessages(Collection<PagerMessage> messages, Collection<TransmitterGroup> groups) {
		getTransmitterNames(groups).forEach(n -> sendMessages(messages, n));
	}

	/**
	 * Sends a message to a specific connected transmitter.
	 * 
	 * @param message         Message to send.
	 * @param transmitterName Transmitter name.
	 */
	public void sendMessage(PagerMessage message, String transmitterName) {
		TransmitterClient cl = connectedClients.get(NamedObject.normalizeName(transmitterName));
		if (cl != null) {
			cl.sendMessage(message);
		}
	}

	/**
	 * Sends a message to a specific connected transmitter only if it is in one of
	 * the given transmitter groups.
	 * 
	 * @param message         Message to send.
	 * @param transmitterName Transmitter name.
	 * @param groups          Transmitter groups to check.
	 * @return {@code true} if the message was sent.
	 */
	public boolean sendMessageIfInGroups(PagerMessage message, String transmitterName,
			Collection<TransmitterGroup> groups) {
		Collection<String> transmitters = getTransmitterNames(groups);
		for (String name : transmitters) {
			if (name.equalsIgnoreCase(transmitterName)) {
				TransmitterClient cl = connectedClients.get(NamedObject.normalizeName(transmitterName));
				if (cl != null) {
					cl.sendMessage(message);
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Sends multiple messages to a specific connected transmitter.
	 * 
	 * @param messages        Messages to send.
	 * @param transmitterName Transmitter name.
	 */
	public void sendMessages(Collection<PagerMessage> messages, String transmitterName) {
		TransmitterClient cl = connectedClients.get(NamedObject.normalizeName(transmitterName));
		if (cl != null) {
			cl.sendMessages(messages);
		}
	}

	/**
	 * Sends a message containing the callsign to each connected transmitter.
	 */
	public void sendCallSigns() {
		connectedClients.values().forEach(tx -> {
			try {
				tx.sendCallSignMessage();
			} catch (Throwable cause) {
				logger.error("Failed to send callsign to transmitter.", cause);
			}
		});
	}

	/**
	 * Callback to handle connect events.
	 * 
	 * @param client Transmitter client to add.
	 */
	public void onConnect(TransmitterClient client) {
		Transmitter t = client.getTransmitter();
		if (t == null) {
			logger.warn("Client has no associated transmitter object.");
			client.close();
			return;
		}

		Lock lock = stateManager.getLock().writeLock();
		lock.lock();

		try {
			t.setStatus(Status.ONLINE);

			Instant lastConnected = Instant.now();
			t.setLastConnected(lastConnected);
			t.setConnectedSince(lastConnected);
		} finally {
			lock.unlock();
		}

		connectedClients.put(t.getNormalizedName(), client);

		notifyStatusChanged(listener, t);
	}

	/**
	 * Callback that handles disconnect events.
	 * 
	 * @param client Transmitter to remove.
	 */
	public void onDisconnect(TransmitterClient client) {
		final Transmitter t = client.getTransmitter();
		if (t == null) {
			return;
		}

		Lock lock = stateManager.getLock().writeLock();
		lock.lock();

		try {
			if (t.getStatus() != Status.ERROR) {
				t.setStatus(Status.OFFLINE);
			}

			t.setConnectedSince(null);
		} finally {
			lock.unlock();
		}

		connectedClients.remove(t.getNormalizedName());

		notifyStatusChanged(listener, t);
	}

	private static void notifyStatusChanged(TransmitterManagerListener listener, Transmitter t) {
		if (listener != null) {
			listener.handleTransmitterStatusChanged(t);
		}
	}

	private static Set<String> getTransmitterNames(Collection<TransmitterGroup> groups) {
		Set<String> selected = new HashSet<>();
		for (TransmitterGroup g : groups) {
			g.getTransmitterNames().forEach(t -> selected.add(NamedObject.normalizeName(t)));
		}

		return selected;
	}

	/**
	 * Disconnects from all connected transmitters.
	 */
	public void disconnectFromAll() {
		connectedClients.values().forEach(cl -> cl.close());

		TransmitterManagerListener theListener = listener;
		if (theListener != null) {
			theListener.handleDisconnectFromAllTransmitters();
		}
	}

	/**
	 * Disconnects from the given transmitter.
	 * 
	 * @param t Transmitter to disconnect from.
	 */
	public void disconnectFrom(Transmitter t) {
		TransmitterClient cl = connectedClients.remove(t.getNormalizedName());
		if (cl != null) {
			cl.close();
		}
	}
}
