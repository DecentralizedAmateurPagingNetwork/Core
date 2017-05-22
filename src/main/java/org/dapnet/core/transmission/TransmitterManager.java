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
	private final ConcurrentMap<String, TransmitterClient> connectedClients = new ConcurrentHashMap<>();
	private volatile TransmitterManagerListener listener;

	/**
	 * Gets a transmitter by its name.
	 * 
	 * @param name
	 *            Transmitter name
	 * @return Transmitter or {@code null} if not found.
	 */
	public Transmitter getTransmitter(String name) {
		TransmitterManagerListener theListener = listener;
		if (theListener != null) {
			return theListener.handleGetTransmitter(name);
		} else {
			return null;
		}
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

		notifyStatusChanged(listener, t);
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
