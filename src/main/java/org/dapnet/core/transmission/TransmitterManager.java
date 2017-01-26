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
	private final Set<TransmitterClient> connectedClients = new HashSet<>();
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
			// TODO Disconnect
			logger.info("Transmitter removed.");
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
			connectedClients.forEach(c -> {
				c.sendMessage(message);
			});
		}
	}

	public void sendMessage(Message message, Collection<TransmitterGroup> groups) {
		Set<String> names = getTransmitterNames(groups);
		synchronized (connectedClients) {
			for (TransmitterClient cl : connectedClients) {
				if (names.equals(cl.getTransmitter().getName())) {
					cl.sendMessage(message);
				}
			}
		}
	}

	public void sendMessages(Collection<Message> messages, Collection<TransmitterGroup> groups) {
		Set<String> names = getTransmitterNames(groups);
		synchronized (connectedClients) {
			for (TransmitterClient cl : connectedClients) {
				if (names.equals(cl.getTransmitter().getName())) {
					cl.sendMessages(messages);
				}
			}
		}
	}

	public void onConnect(TransmitterClient client) {
		Transmitter t = client.getTransmitter();
		if (t != null) {
			t.setStatus(Status.ONLINE);
		}

		synchronized (connectedClients) {
			connectedClients.add(client);
		}

		notifyStatusChanged(t);
	}

	public void onDisconnect(TransmitterClient client) {
		Transmitter t = client.getTransmitter();
		if (t != null && t.getStatus() != Status.ERROR) {
			t.setStatus(Status.OFFLINE);
		}

		synchronized (connectedClients) {
			connectedClients.remove(client);
		}

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
			connectedClients.forEach(c -> c.close());
		}

		if (listener != null) {
			listener.handleDisconnectedFromAllTransmitters();
		}
	}

	public void disconnectFrom(Transmitter t) {
		synchronized (connectedClients) {
			for (TransmitterClient cl : connectedClients) {
				if (t.getName().equals(cl.getTransmitter().getName())) {
					cl.close();
					// Could there be more than one transmitter?
					break;
				}
			}
		}
	}

	public void updateTransmitter(Transmitter oldTransmitter, Transmitter newTransmitter) {

	}
}
