package org.dapnet.core.transmission;

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

public class TransmitterManager {

	private static final Logger logger = LogManager.getLogger();
	private final ConcurrentMap<String, Transmitter> registeredTranmsitters = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, TransmitterClient> connectedClients = new ConcurrentHashMap<>();
	private TransmitterManagerListener listener;

	public void addTransmitter(Transmitter transmitter) {
		String name = transmitter.getName().toLowerCase();

		registeredTranmsitters.put(name, transmitter);
		logger.info("Transmitter added: {}", name);
	}

	public void removeTransmitter(Transmitter transmitter) {
		String name = transmitter.getName().toLowerCase();

		if (registeredTranmsitters.remove(name) != null) {
			logger.info("Transmitter removed: {}", name);
		} else {
			logger.warn("Transmitter is not registered: {}", name);
		}

		disconnectFrom(transmitter);
	}

	public void setListener(TransmitterManagerListener listener) {
		this.listener = listener;
	}

	public void addTransmitters(Collection<Transmitter> transmitters) {
		transmitters.forEach(this::addTransmitter);
	}

	public Transmitter get(String name) {
		if (name != null) {
			name = name.toLowerCase();
		}

		return registeredTranmsitters.get(name);
	}

	public void sendMessage(Message message) {
		connectedClients.values().forEach(c -> {
			c.sendMessage(message);
		});
	}

	public void sendMessage(Message message, Collection<TransmitterGroup> groups) {
		Set<String> names = getTransmitterNames(groups);
		for (String name : names) {
			// name is already in lower case
			TransmitterClient cl = connectedClients.get(name);
			if (cl != null) {
				cl.sendMessage(message);
			}
		}
	}

	public void sendMessages(Collection<Message> messages, Collection<TransmitterGroup> groups) {
		Set<String> names = getTransmitterNames(groups);
		for (String name : names) {
			TransmitterClient cl = connectedClients.get(name);
			if (cl != null) {
				cl.sendMessages(messages);
			}
		}
	}

	public void onConnect(TransmitterClient client) {
		Transmitter t = client.getTransmitter();
		if (t == null) {
			logger.warn("Client has no associated transmitter object.");
			return;
		}

		t.setStatus(Status.ONLINE);

		connectedClients.put(t.getName(), client);

		if (listener != null) {
			listener.handleTransmitterStatusChanged(t);
		}
	}

	public void onDisconnect(TransmitterClient client) {
		Transmitter t = client.getTransmitter();
		if (t == null) {
			logger.warn("Client has no associated transmitter object.");
			return;
		}

		if (t.getStatus() != Status.ERROR) {
			t.setStatus(Status.OFFLINE);
		}

		connectedClients.remove(t.getName());

		if (listener != null) {
			listener.handleTransmitterStatusChanged(t);
		}
	}

	private Set<String> getTransmitterNames(Collection<TransmitterGroup> groups) {
		Set<String> selected = new HashSet<>();
		for (TransmitterGroup g : groups) {
			g.getTransmitterNames().forEach(t -> selected.add(t.toLowerCase()));
		}

		return selected;
	}

	public void disconnectFromAll() {
		connectedClients.values().forEach(cl -> cl.close());

		if (listener != null) {
			listener.handleDisconnectedFromAllTransmitters();
		}
	}

	public void disconnectFrom(Transmitter t) {
		TransmitterClient cl = connectedClients.remove(t.getName().toLowerCase());
		if (cl != null) {
			cl.close();
		}
	}

}
