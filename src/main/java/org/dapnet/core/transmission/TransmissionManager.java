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

package org.dapnet.core.transmission;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.Settings;
import org.dapnet.core.model.Activation;
import org.dapnet.core.model.Call;
import org.dapnet.core.model.CoreRepository;
import org.dapnet.core.model.News;
import org.dapnet.core.model.Pager;
import org.dapnet.core.model.Rubric;
import org.dapnet.core.model.StateManager;
import org.dapnet.core.model.Transmitter;
import org.dapnet.core.model.TransmitterGroup;
import org.dapnet.core.transmission.messages.DefaultPagerProtocolFactory;
import org.dapnet.core.transmission.messages.PagerMessage;
import org.dapnet.core.transmission.messages.PagerMessageFactory;
import org.dapnet.core.transmission.messages.PagerProtocol;
import org.dapnet.core.transmission.messages.PagerProtocolFactory;
import org.dapnet.core.transmission.messages.TransmitterIdentification;

/**
 * This class implements the transmission manager responsible for sending
 * messages to transmitters.
 * 
 * @author Philipp Thiel
 */
public class TransmissionManager {
	private static final Logger logger = LogManager.getLogger();
	private final ConcurrentMap<Pager.Type, PagerProtocol> pagerProtocols = new ConcurrentHashMap<>();
	private final TransmitterManager transmitterManager;

	/**
	 * Constructs a new transmission manager instance.
	 * 
	 * @param Settings     Core settings to use
	 * @param stateManager State manager to use
	 * @throws NullPointerException if the state manager is {@code null}
	 */
	public TransmissionManager(Settings settings, StateManager stateManager) {
		transmitterManager = new TransmitterManager(settings, stateManager);

		// Register pager protocols
		PagerProtocolFactory protocolFactory = new DefaultPagerProtocolFactory();
		for (Pager.Type type : Pager.Type.values()) {
			PagerProtocol protocol = protocolFactory.getProtocol(type, settings, stateManager);
			if (protocol != null) {
				pagerProtocols.put(type, protocol);
			} else {
				logger.warn("Pager type '{}' is not supported by protocol factory.", type);
			}
		}
	}

	/**
	 * Sends a time messages to all transmitters. The time messages are created for
	 * all pager protocols that support them.
	 * 
	 * @param time Time to send
	 */
	public void sendTime(ZonedDateTime time) {
		if (time == null) {
			throw new NullPointerException("Time must not be null.");
		}

		final Collection<PagerMessage> messages = new LinkedList<>();
		for (PagerProtocol protocol : pagerProtocols.values()) {
			addMessagesFromProtocol(time, protocol.getTimeFactory(), messages);
		}

		try {
			transmitterManager.sendMessages(messages);
		} catch (Exception ex) {
			logger.error("Failed to send messages.", ex);
		}
	}

	/**
	 * Sends news messages for pagers that support rubrics to all transmitter groups
	 * belonging to the rubric in the news. This will not create messages for pagers
	 * that do not support rubrics, see {@link handeNewsAsCall}.
	 * 
	 * @param news News to send
	 */
	public void sendNewsAsRubric(News news) {
		if (news == null) {
			throw new NullPointerException("News must not be null.");
		}

		Set<String> transmitterGroups = null;

		final CoreRepository repo = transmitterManager.getRepository();
		Lock lock = repo.getLock().readLock();
		lock.lock();

		try {
			Rubric rubric = repo.getRubrics().get(news.getRubricName());
			if (rubric != null) {
				transmitterGroups = new TreeSet<>(rubric.getTransmitterGroupNames());
			}
		} finally {
			lock.unlock();
		}

		if (transmitterGroups == null) {
			logger.error("Failed to send news, could not get transmitter groups for rubric '{}'.",
					news.getRubricName());
			return;
		}

		final Collection<PagerMessage> messages = new LinkedList<>();
		for (PagerProtocol protocol : pagerProtocols.values()) {
			if (protocol.getRubricFactory() != null) {
				addMessagesFromProtocol(news, protocol.getNewsFactory(), messages);
			} else {
				logger.debug("Pager protocol for '{}' does not support sending news to rubrics.",
						protocol.getPagerType());
			}
		}

		try {
			transmitterManager.sendMessages(messages, transmitterGroups);

			logger.info("News sent to transmitters.");
		} catch (Exception e) {
			logger.error("Failed to send news", e);
		}
	}

	/**
	 * Sends a news message as a regular call to all transmitter groups belonging to
	 * the news. This will not create messages for pagers that support rubrics, see
	 * {@link handeNewsAsRubric}.
	 * 
	 * @param news News to send
	 */
	public void sendNewsAsCall(News news) {
		if (news == null) {
			throw new NullPointerException("News must not be null.");
		}

		Set<String> transmitterGroups = null;

		final CoreRepository repo = transmitterManager.getRepository();
		Lock lock = repo.getLock().readLock();
		lock.lock();

		try {
			Rubric rubric = repo.getRubrics().get(news.getRubricName());
			if (rubric != null) {
				transmitterGroups = new TreeSet<>(rubric.getTransmitterGroupNames());
			}
		} finally {
			lock.unlock();
		}

		if (transmitterGroups == null) {
			logger.error("Failed to send news, could not get transmitter groups for rubric '{}'.",
					news.getRubricName());
			return;
		}

		final Collection<PagerMessage> messages = new LinkedList<>();
		for (PagerProtocol protocol : pagerProtocols.values()) {
			if (protocol.getRubricFactory() == null) {
				addMessagesFromProtocol(news, protocol.getNewsFactory(), messages);
			} else {
				logger.debug("Pager protocol for '{}' does not require sending news as calls.",
						protocol.getPagerType());
			}
		}

		try {
			transmitterManager.sendMessages(messages, transmitterGroups);

			logger.info("News sent to transmitters.");
		} catch (Exception e) {
			logger.error("Failed to send news", e);
		}
	}

	/**
	 * Sends a rubric message to all transmitter groups belonging to the rubric.
	 * This will only create messages for pagers that support rubrics.
	 * 
	 * @param rubric Rubric to send
	 */
	public void sendRubric(Rubric rubric) {
		if (rubric == null) {
			throw new NullPointerException("Rubric must not be null.");
		}

		final Collection<PagerMessage> messages = new LinkedList<>();
		for (PagerProtocol protocol : pagerProtocols.values()) {
			addMessagesFromProtocol(rubric, protocol.getRubricFactory(), messages);
		}

		try {
			transmitterManager.sendMessages(messages, rubric.getTransmitterGroupNames());

			logger.info("Rubric {} sent to transmitters.", rubric.getName());
		} catch (Exception e) {
			logger.error("Failed to send rubric " + rubric.getName(), e);
		}
	}

	/**
	 * Sends a rubric message to the specified transmitter. This will only create
	 * messages for pagers that support rubrics.
	 * 
	 * @param rubric          Rubric to send
	 * @param transmitterName Transmitter name
	 */
	public void sendRubricToTransmitter(Rubric rubric, String transmitterName) {
		if (rubric == null) {
			throw new NullPointerException("Rubric must not be null.");
		}

		if (transmitterName == null) {
			throw new NullPointerException("Transmitter name must not be null.");
		}

		final Collection<PagerMessage> messages = new LinkedList<>();
		for (PagerProtocol protocol : pagerProtocols.values()) {
			addMessagesFromProtocol(rubric, protocol.getRubricFactory(), messages);
		}

		try {
			if (transmitterManager.sendMessagesIfInGroups(messages, transmitterName,
					rubric.getTransmitterGroupNames())) {
				logger.info("Rubric {} sent to transmitter {}", rubric.getName(), transmitterName);
			}
		} catch (Exception ex) {
			logger.error("Failed to send rubric " + rubric.getName() + " to transmitter " + transmitterName, ex);
		}
	}

	/**
	 * Sends a call message to the transmitter groups belonging to the call.
	 * 
	 * @param call Call to send
	 */
	public void sendCall(Call call) {
		if (call == null) {
			throw new NullPointerException("Call must not be null.");
		}

		final Collection<PagerMessage> messages = new LinkedList<>();
		for (PagerProtocol protocol : pagerProtocols.values()) {
			addMessagesFromProtocol(call, protocol.getCallFactory(), messages);
		}

		try {
			transmitterManager.sendMessages(messages, call.getTransmitterGroupNames());

			logger.info("Call sent to {} CallSigns, to {} Pagers, using {} TransmitterGroups.",
					call.getCallSignNames().size(), messages.size(), call.getTransmitterGroupNames().size());

			// XXX No other easy way of doing this without performing a cluster-wide remote
			// procedure call
			final CoreRepository repo = transmitterManager.getRepository();
			Lock lock = repo.getLock().writeLock();
			lock.lock();

			try {
				Set<Transmitter> transmitters = new HashSet<>();
				Set<TransmitterGroup> transmitterGroups = repo.getTransmitterGroups()
						.get(call.getTransmitterGroupNames());
				for (TransmitterGroup tg : transmitterGroups) {
					Set<Transmitter> selected = repo.getTransmitters().get(tg.getTransmitterNames());
					transmitters.addAll(selected);
				}

				transmitters.forEach(t -> t.updateCallCount(1));
			} finally {
				lock.unlock();
			}
		} catch (Exception e) {
			logger.error("Failed to send call.", e);
		}
	}

	/**
	 * Sends an activation message to the transmitter groups belonging to the
	 * activation. This will only create messages for pagers that require
	 * activation.
	 * 
	 * @param activation Activation to send
	 */
	public void sendActivation(Activation activation) {
		if (activation == null) {
			throw new NullPointerException("Activation must not be null.");
		}

		final Collection<PagerMessage> messages = new LinkedList<>();
		for (PagerProtocol protocol : pagerProtocols.values()) {
			addMessagesFromProtocol(activation, protocol.getActivationFactory(), messages);
		}

		try {
			transmitterManager.sendMessages(messages, activation.getTransmitterGroupNames());

			logger.info("Activation sent to transmitters.");
		} catch (Exception e) {
			logger.error("Failed to send activation.", e);
		}

		// Send info message
		// FIXME This is broken, we need to set the callsign
		// try {
		// Call call = new Call();
		// call.setTimestamp(Instant.now());
		// call.setText("Your skyper has been activated.");
		// call.setOwnerName("admin");
		// call.setTransmitterGroupNames(activation.getTransmitterGroupNames());
		//
		// List<PagerMessage> messages = protocol.createMessagesFromCall(call);
		// transmitterManager.sendMessages(messages, call.getTransmitterGroups());
		//
		// logger.info("Activation info message sent to transmitters.");
		// } catch (Exception e) {
		// logger.error("Failed to send activation info message", e);
		// }
	}

	/**
	 * Sends transmitter identification messages for all connected transmitters.
	 */
	public void sendIdentification() {
		Lock lock = transmitterManager.getRepository().getLock().readLock();
		lock.lock();

		try {
			final Collection<TransmitterClient> clients = transmitterManager.getConnectedClients();
			for (TransmitterClient client : clients) {
				final Transmitter tx = client.getTransmitter();
				if (tx == null) {
					logger.warn("Transmitter data not set, cannot send identification.");
					continue;
				}

				final Collection<PagerMessage> messages = new LinkedList<>();
				final TransmitterIdentification id = new TransmitterIdentification(tx.getName(),
						tx.getIdentificationAddress());
				for (PagerProtocol proto : pagerProtocols.values()) {
					addMessagesFromProtocol(id, proto.getTransmitterIdentificationFactory(), messages);
				}

				client.sendMessages(messages);
			}

			logger.info("Transmitter identifications sent.");
		} catch (Exception e) {
			logger.error("Failed to send transmitter identifications.", e);
		} finally {
			lock.unlock();
		}
	}

	public TransmitterManager getTransmitterManager() {
		return transmitterManager;
	}

	private <T> void addMessagesFromProtocol(T object, PagerMessageFactory<T> factory,
			Collection<PagerMessage> messages) {
		if (factory == null) {
			logger.debug("Object type '{}' not supported by protocol.", object.getClass());
			return;
		}

		try {
			Collection<PagerMessage> created = factory.createMessage(object);
			if (created != null) {
				messages.addAll(created);
			} else {
				logger.warn("No messages created for type '{}'", object.getClass());
			}
		} catch (Exception ex) {
			logger.error("Failed to create messages.", ex);
		}
	}
}
