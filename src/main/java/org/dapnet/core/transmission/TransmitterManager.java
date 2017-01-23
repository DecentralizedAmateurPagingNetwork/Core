package org.dapnet.core.transmission;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.dapnet.core.model.Transmitter;

public class TransmitterManager {

	private final Map<String, Transmitter> registeredTranmsitters = new ConcurrentHashMap<>();

	public void registerTransmitter(Transmitter transmitter) {
		String key = transmitter.getAuthKey();
		if (key != null && !key.isEmpty()) {
			registeredTranmsitters.put(key, transmitter);
		}
	}

	public void registerTransmitters(Collection<Transmitter> transmitters) {
		transmitters.forEach(this::registerTransmitter);
	}

	public Transmitter auth(String authKey) {
		return registeredTranmsitters.get(authKey);
	}
}
