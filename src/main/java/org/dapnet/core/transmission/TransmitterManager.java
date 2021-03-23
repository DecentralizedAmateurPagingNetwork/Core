package org.dapnet.core.transmission;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.Settings;
import org.dapnet.core.model.CoreRepository;
import org.dapnet.core.model.ModelRepository;
import org.dapnet.core.model.NamedObject;
import org.dapnet.core.model.Transmitter;
import org.dapnet.core.model.Transmitter.Status;
import org.dapnet.core.model.TransmitterGroup;
import org.dapnet.core.transmission.messages.PagerMessage;

/**
 * This class manages connected transmitters.
 * 
 * @author Philipp Thiel
 */
public class TransmitterManager {
	private static final Logger logger = LogManager.getLogger();
	private final ConcurrentMap<String, TransmitterClient> connectedClients = new ConcurrentHashMap<>();
	private final Settings settings;
	private final CoreRepository repository;
	private volatile TransmitterManagerListener listener;

	/**
	 * Constructs a new transmitter manager instance.
	 * 
	 * @param repository Repository instance to use
	 * @throws NullPointerException if the repository is {@code null}
	 */
	public TransmitterManager(Settings settings, CoreRepository repository) {
		this.settings = Objects.requireNonNull(settings, "Settings must not be null.");
		this.repository = Objects.requireNonNull(repository, "Repository must not be null.");
	}

	/**
	 * Gets the Core settings instance.
	 * 
	 * @return Settings
	 */
	public Settings getSettings() {
		return settings;
	}

	/**
	 * Gets the repository instance.
	 * 
	 * @return Repository
	 */
	public CoreRepository getRepository() {
		return repository;
	}

	/**
	 * Gets an unmodifyable collection of the currently connected clients.
	 * 
	 * @return Collection of transmitter clients
	 */
	public Collection<TransmitterClient> getConnectedClients() {
		return Collections.unmodifiableCollection(connectedClients.values());
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
	 * Sends messages to all connected transmitters.
	 * 
	 * @param messages Messages to send
	 */
	public void sendMessages(Collection<PagerMessage> messages) {
		connectedClients.values().forEach(c -> c.sendMessages(messages));
	}

	/**
	 * Sends a message to one or more transmitter groups.
	 * 
	 * @param message               Message to send.
	 * @param transmitterGroupNames Transmitter groups to send to.
	 */
	public void sendMessage(PagerMessage message, Set<String> transmitterGroupNames) {
		Set<String> transmitters = null;

		Lock lock = repository.getLock().readLock();
		lock.lock();

		try {
			transmitters = getTransmitterNames(transmitterGroupNames);
		} finally {
			lock.unlock();
		}

		transmitters.forEach(n -> sendMessage(message, n));
	}

	/**
	 * Sends multiple messages to one or more transmitter groups.
	 * 
	 * @param messages              Messages to send.
	 * @param transmitterGroupNames Transmitter groups to send to.
	 */
	public void sendMessages(Collection<PagerMessage> messages, Set<String> transmitterGroupNames) {
		Set<String> transmitters = null;

		Lock lock = repository.getLock().readLock();
		lock.lock();

		try {
			transmitters = getTransmitterNames(transmitterGroupNames);
		} finally {
			lock.unlock();
		}

		transmitters.forEach(n -> sendMessages(messages, n));
	}

	/**
	 * Sends a message to a specific connected transmitter.
	 * 
	 * @param message         Message to send.
	 * @param transmitterName Transmitter name.
	 */
	public void sendMessage(PagerMessage message, String transmitterName) {
		TransmitterClient cl = connectedClients.get(NamedObject.normalize(transmitterName));
		if (cl != null) {
			cl.sendMessage(message);
		}
	}

	/**
	 * Sends a message to a specific connected transmitter only if it is in one of
	 * the given transmitter groups.
	 * 
	 * @param message               Message to send.
	 * @param transmitterName       Transmitter name.
	 * @param transmitterGroupNames Transmitter groups to check.
	 * @return {@code true} if the message was sent.
	 */
	public boolean sendMessageIfInGroups(PagerMessage message, String transmitterName,
			Set<String> transmitterGroupNames) {
		Set<String> transmitters = null;

		Lock lock = repository.getLock().readLock();
		lock.lock();

		try {
			transmitters = getTransmitterNames(transmitterGroupNames);
		} finally {
			lock.unlock();
		}

		transmitterName = NamedObject.normalize(transmitterName);
		if (transmitters.contains(transmitterName)) {
			TransmitterClient cl = connectedClients.get(transmitterName);
			if (cl != null) {
				cl.sendMessage(message);
				return true;
			}
		}

		return false;
	}

	/**
	 * Sends messages to a specific connected transmitter only if it is in one of
	 * the given transmitter groups.
	 * 
	 * @param messages              Messages to send.
	 * @param transmitterName       Transmitter name.
	 * @param transmitterGroupNames Transmitter groups to check.
	 * @return {@code true} if the message was sent.
	 */
	public boolean sendMessagesIfInGroups(Collection<PagerMessage> messages, String transmitterName,
			Set<String> transmitterGroupNames) {
		Set<String> transmitters = null;

		Lock lock = repository.getLock().readLock();
		lock.lock();

		try {
			transmitters = getTransmitterNames(transmitterGroupNames);
		} finally {
			lock.unlock();
		}

		transmitterName = NamedObject.normalize(transmitterName);
		if (transmitters.contains(transmitterName)) {
			TransmitterClient cl = connectedClients.get(transmitterName);
			if (cl != null) {
				cl.sendMessages(messages);
				return true;
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
		TransmitterClient cl = connectedClients.get(NamedObject.normalize(transmitterName));
		if (cl != null) {
			cl.sendMessages(messages);
		}
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
			client.close().syncUninterruptibly();
			return;
		}

		Lock lock = repository.getLock().writeLock();
		lock.lock();

		try {
			t.setStatus(Status.ONLINE);

			Instant lastConnected = Instant.now();
			t.setLastConnected(lastConnected);
			t.setConnectedSince(lastConnected);
		} finally {
			lock.unlock();
		}

		connectedClients.put(NamedObject.normalize(t.getName()), client);

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

		Lock lock = repository.getLock().writeLock();
		lock.lock();

		try {
			if (t.getStatus() != Status.ERROR) {
				t.setStatus(Status.OFFLINE);
			}

			t.setConnectedSince(null);
		} finally {
			lock.unlock();
		}

		connectedClients.remove(NamedObject.normalize(t.getName()));

		notifyStatusChanged(listener, t);
	}

	private static void notifyStatusChanged(TransmitterManagerListener listener, Transmitter t) {
		if (listener != null) {
			listener.handleTransmitterStatusChanged(t);
		}
	}

	private Set<String> getTransmitterNames(Set<String> transmitterGroupNames) {
		if (transmitterGroupNames == null) {
			return null;
		}

		Set<String> result = new TreeSet<>();
		if (!transmitterGroupNames.isEmpty()) {
			final ModelRepository<TransmitterGroup> repo = repository.getTransmitterGroups();

			for (String name : transmitterGroupNames) {
				TransmitterGroup tg = repo.get(name);
				if (tg != null) {
					Set<String> transmitters = tg.getTransmitterNames();
					if (transmitters != null) {
						transmitters.forEach(t -> result.add(NamedObject.normalize(t)));
					}
				}
			}
		}

		return result;
	}

	/**
	 * Disconnects from all connected transmitters.
	 */
	public void disconnectFromAll() {
		connectedClients.values().forEach(cl -> cl.close().syncUninterruptibly());

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
		TransmitterClient cl = connectedClients.remove(NamedObject.normalize(t.getName()));
		if (cl != null) {
			cl.close().syncUninterruptibly();
		}
	}

}
