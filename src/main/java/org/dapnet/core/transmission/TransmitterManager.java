package org.dapnet.core.transmission;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
	private final ConcurrentMap<String, Transmitter> registeredTranmsitters = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, TransmitterClient> connectedClients = new ConcurrentHashMap<>();
	private volatile TransmitterManagerListener listener;

	/**
	 * Registers a transmitter so it is allowed to connect to the server.
	 * 
	 * @param transmitter
	 *            Transmitter to register.
	 */
	public void addTransmitter(Transmitter transmitter) {
		String name = transmitter.getName().toLowerCase();

		registeredTranmsitters.put(name, transmitter);
		logger.info("Transmitter added: {}", name);
	}

	/**
	 * Removes a transmitter from the list of known transmitters. If a
	 * connection is established it will be closed.
	 * 
	 * @param transmitter
	 *            Transmitter to remove.
	 */
	public void removeTransmitter(Transmitter transmitter) {
		String name = transmitter.getName().toLowerCase();

		if (registeredTranmsitters.remove(name) != null) {
			logger.info("Transmitter removed: {}", name);
		} else {
			logger.warn("Transmitter is not registered: {}", name);
		}

		disconnectFrom(transmitter);
	}

	/**
	 * Sets the event listener.
	 * 
	 * @param listener
	 *            Event listener instance.
	 */
	public void setListener(TransmitterManagerListener listener) {
		this.listener = listener;
	}

	/**
	 * Adds a list of transmitters.
	 * 
	 * @param transmitters
	 *            Transmitters to add.
	 */
	public void addTransmitters(Collection<Transmitter> transmitters) {
		transmitters.forEach(this::addTransmitter);
	}

	/**
	 * Returns a transmitter if it is registered.
	 * 
	 * @param name
	 *            Transmitter name
	 * @return Transmitter instance or {@code null} if no transmitter is found.
	 */
	public Transmitter get(String name) {
		if (name != null) {
			name = name.toLowerCase();
		}

		return registeredTranmsitters.get(name);
	}

	/**
	 * Sends a message to all connected transmitters.
	 * 
	 * @param message
	 *            Message to send.
	 */
	public void sendMessage(Message message) {
		connectedClients.values().forEach(c -> c.sendMessage(message));
	}

	/**
	 * Sends a message to one or more transmitter groups.
	 * 
	 * @param message
	 *            Message to send.
	 * @param groups
	 *            Transmitter groups to send to.
	 */
	public void sendMessage(Message message, Collection<TransmitterGroup> groups) {
		getTransmitterNames(groups).forEach(n -> sendMessage(message, n));
	}

	/**
	 * Sends multiple messages to one or more transmitter groups.
	 * 
	 * @param messages
	 *            Messages to send.
	 * @param groups
	 *            Transmitter groups to send to.
	 */
	public void sendMessages(Collection<Message> messages, Collection<TransmitterGroup> groups) {
		getTransmitterNames(groups).forEach(n -> sendMessages(messages, n));
	}

	/**
	 * Sends a message to a specific connected transmitter.
	 * 
	 * @param message
	 *            Message to send.
	 * @param transmitterName
	 *            Transmitter name.
	 */
	public void sendMessage(Message message, String transmitterName) {
		TransmitterClient cl = connectedClients.get(transmitterName);
		if (cl != null) {
			cl.sendMessage(message);
		}
	}

	/**
	 * Sends multiple messages to a specific connected transmitter.
	 * 
	 * @param messages
	 *            Messages to send.
	 * @param transmitterName
	 *            Transmitter name.
	 */
	public void sendMessages(Collection<Message> messages, String transmitterName) {
		TransmitterClient cl = connectedClients.get(transmitterName);
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
	 * @param client
	 *            Transmitter client to add.
	 */
	public void onConnect(TransmitterClient client) {
		Transmitter t = client.getTransmitter();
		if (t == null) {
			logger.warn("Client has no associated transmitter object.");
			client.close();
			return;
		}

		t.setStatus(Status.ONLINE);

		Instant lastConnected = Instant.now();
		t.setLastConnected(lastConnected);
		t.setConnectedSince(lastConnected);

		connectedClients.put(t.getName(), client);

		notifyStatusChanged(t);
	}

	/**
	 * Callback that handles disconnect events.
	 * 
	 * @param client
	 *            Transmitter to remove.
	 */
	public void onDisconnect(TransmitterClient client) {
		Transmitter t = client.getTransmitter();
		if (t == null) {
			return;
		}

		if (t.getStatus() != Status.ERROR) {
			t.setStatus(Status.OFFLINE);
		}

		t.setConnectedSince(null);

		connectedClients.remove(t.getName());

		notifyStatusChanged(t);
	}

	private void notifyStatusChanged(Transmitter t) {
		TransmitterManagerListener theListener = listener;
		if (theListener != null) {
			theListener.handleTransmitterStatusChanged(t);
		}
	}

	private Set<String> getTransmitterNames(Collection<TransmitterGroup> groups) {
		Set<String> selected = new HashSet<>();
		for (TransmitterGroup g : groups) {
			g.getTransmitterNames().forEach(t -> selected.add(t.toLowerCase()));
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
			theListener.handleDisconnectedFromAllTransmitters();
		}
	}

	/**
	 * Disconnects from the given transmitter.
	 * 
	 * @param t
	 *            Transmitter to disconnect from.
	 */
	public void disconnectFrom(Transmitter t) {
		TransmitterClient cl = connectedClients.remove(t.getName().toLowerCase());
		if (cl != null) {
			cl.close();
		}
	}
}
