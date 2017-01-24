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

public class TransmitterManager {

	private static final Logger logger = LogManager.getLogger(TransmitterManager.class);
	private final Map<String, Transmitter> registeredTranmsitters = new ConcurrentHashMap<>();
	private final Set<TransmitterClient> connectedClients = new HashSet<>();

	public void registerTransmitter(Transmitter transmitter) {
		String key = transmitter.getAuthKey();
		if (key != null && !key.isEmpty()) {
			registeredTranmsitters.put(key, transmitter);
			logger.info("Transmitter registered.");
		}
	}

	public void registerTransmitters(Collection<Transmitter> transmitters) {
		transmitters.forEach(this::registerTransmitter);
	}

	public Transmitter get(String key) {
		return registeredTranmsitters.get(key);
	}

	public void onConnect(TransmitterClient client) {
		Transmitter t = client.getTransmitter();
		if (t != null) {
			t.setStatus(Status.ONLINE);
		}

		synchronized (connectedClients) {
			connectedClients.add(client);
		}
	}

	public void onDisconnect(TransmitterClient client) {
		Transmitter t = client.getTransmitter();
		if (t != null) {
			t.setStatus(Status.OFFLINE);
		}

		synchronized (connectedClients) {
			connectedClients.remove(client);
		}
	}

}
