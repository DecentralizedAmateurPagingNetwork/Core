package org.dapnet.core.transmission;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.model.Transmitter;
import org.dapnet.core.model.Transmitter.Status;
import org.dapnet.core.model.TransmitterGroup;

public class TransmitterManager {

	private static final Logger logger = LogManager.getLogger(TransmitterManager.class);
	private final Map<String, Transmitter> registeredTranmsitters = new ConcurrentHashMap<>();
	private final Map<String, TransmitterClient> connectedClients = new ConcurrentHashMap<>();
	private TransmitterManagerListener listener;

	public void addTransmitter(Transmitter transmitter) {
		String key = transmitter.getAuthKey();
		if (key != null && !key.isEmpty()) {
			registeredTranmsitters.put(key, transmitter);
			logger.info("Transmitter registered.");
		}
	}

	public void removeTransmitter(Transmitter transmitter) {
		String key = transmitter.getAuthKey();
		if (key != null && !key.isEmpty()) {
			registeredTranmsitters.remove(key);
			logger.info("Transmitter removed.");

			disconnectFrom(transmitter);
		}
	}

	public void setListener(TransmitterManagerListener listener) {
		this.listener = listener;
	}

	public void addTransmitters(Collection<Transmitter> transmitters) {
		transmitters.forEach(this::addTransmitter);
	}

	public Transmitter get(String key) {
		return registeredTranmsitters.get(key);
	}

	public void sendMessage(Message message) {
		synchronized (connectedClients) {
			connectedClients.values().forEach(c -> {
				c.sendMessage(message);
			});
		}
	}

	public void sendMessage(Message message, Collection<TransmitterGroup> groups) {
		Set<String> names = getTransmitterNames(groups);
		for (String name : names) {
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

		notifyStatusChanged(t);
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

		notifyStatusChanged(t);
	}

	private void notifyStatusChanged(Transmitter t) {
		if (listener != null && t != null) {
			listener.handleTransmitterStatusChanged(t.getName(), t.getStatus());
		}
	}

	private Set<String> getTransmitterNames(Collection<TransmitterGroup> groups) {
		Set<String> selected = new HashSet<>();
		for (TransmitterGroup g : groups) {
			selected.addAll(g.getTransmitterNames());
		}

		return selected;
	}

	public void disconnectFromAll() {
		synchronized (connectedClients) {
			connectedClients.values().forEach(cl -> cl.close());
		}

		if (listener != null) {
			listener.handleDisconnectedFromAllTransmitters();
		}
	}

	public void disconnectFrom(Transmitter t) {
		TransmitterClient cl = connectedClients.remove(t.getName());
		if (cl != null) {
			cl.close();
		}
	}

}
