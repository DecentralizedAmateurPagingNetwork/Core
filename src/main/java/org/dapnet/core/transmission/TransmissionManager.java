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

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.model.Activation;
import org.dapnet.core.model.Call;
import org.dapnet.core.model.CoreRepository;
import org.dapnet.core.model.News;
import org.dapnet.core.model.Rubric;
import org.dapnet.core.model.StateManager;
import org.dapnet.core.model.Transmitter;
import org.dapnet.core.model.TransmitterGroup;

public class TransmissionManager {
	private static final Logger logger = LogManager.getLogger();
	private final PagerProtocol protocol;
	private final TransmitterManager transmitterManager;

	public TransmissionManager(StateManager stateManager) {
		protocol = new SkyperProtocol(stateManager);
		transmitterManager = new TransmitterManager(stateManager);
	}

	public void handleTime(LocalDateTime time) {
		// Skyper Time
		try {
			PagerMessage message = protocol.createMessageFromTime(time);
			transmitterManager.sendMessage(message);

			logger.info("Time sent to transmitters in Skyper format.");
		} catch (Exception e) {
			logger.error("Failed to send Time in Skyper format.", e);
		}

		// Swissphone Time
		try {
			PagerMessage message = protocol.createMessageFromTimeSwissphone(time);
			transmitterManager.sendMessage(message);

			logger.info("Time sent to transmitters in Swissphone format.");
		} catch (Exception e) {
			logger.error("Failed to send Time in Swissphone format.", e);
		}

		// AlphaPoc Time
		try {
			PagerMessage message = protocol.createMessageFromTimeAlphaPoc(time);
			transmitterManager.sendMessage(message);

			logger.info("Time sent to transmitters in AlphaPoc format.");
		} catch (Exception e) {
			logger.error("Failed to send Time in AlphaPoc format.", e);
		}

	}

	public void handleLocalTime(LocalDateTime time) {
		// Swissphone Time in local time
		try {
			PagerMessage message = protocol.createMessageFromLocalTimeSwissphone(time);
			transmitterManager.sendMessage(message);

			logger.info("Local time sent to transmitters in Swissphone format.");
		} catch (Exception e) {
			logger.error("Failed to send local time in Swissphone format.", e);
		}

		// AlphaPoc Time in local time
		try {
			PagerMessage message = protocol.createMessageFromLocalTimeAlphaPoc(time);
			transmitterManager.sendMessage(message);

			logger.info("Local time sent to transmitters in AlphaPoc format.");
		} catch (Exception e) {
			logger.error("Failed to send local time in AlphaPoc format.", e);
		}

	}

	public void handleNews(News news) {
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
			logger.error("Failed to send news, could not get rubric for name: {}", news.getRubricName());
			return;
		}

		try {
			PagerMessage message = protocol.createMessageFromNews(news);
			transmitterManager.sendMessage(message, transmitterGroups);

			logger.info("News sent to transmitters.");
		} catch (Exception e) {
			logger.error("Failed to send News", e);
		}
	}

	public void handleNewsAsCall(News news) {
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
			logger.error("Failed to send news, could not get rubric for name: {}", news.getRubricName());
			return;
		}

		try {
			PagerMessage message = protocol.createMessageFromNewsAsCall(news);
			transmitterManager.sendMessage(message, transmitterGroups);

			logger.info("News sent to transmitters as call.");
		} catch (Exception ex) {
			logger.error("Failed to send News as call", ex);
		}
	}

	public void handleRubric(Rubric rubric) {
		try {
			PagerMessage message = protocol.createMessageFromRubric(rubric);
			transmitterManager.sendMessage(message, rubric.getTransmitterGroupNames());

			logger.info("Rubric {} sent to transmitters.", rubric.getName());
		} catch (Exception e) {
			logger.error("Failed to send Rubric " + rubric.getName(), e);
		}
	}

	public void handleRubricToTransmitter(Rubric rubric, String transmitterName) {
		try {
			PagerMessage message = protocol.createMessageFromRubric(rubric);
			if (transmitterManager.sendMessageIfInGroups(message, transmitterName, rubric.getTransmitterGroupNames())) {
				logger.info("Rubric {} sent to transmitter {}", rubric.getName(), transmitterName);
			}
		} catch (Exception ex) {
			logger.error("Failed to send rubric " + rubric.getName() + " to transmitter " + transmitterName, ex);
		}
	}

	public void handleCall(Call call) {
		try {
			List<PagerMessage> messages = protocol.createMessagesFromCall(call);
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
			logger.error("Failed to send Call", e);
		}
	}

	public void handleActivation(Activation activation) {
		try {
			PagerMessage message = protocol.createMessageFromActivation(activation);

			transmitterManager.sendMessage(message, activation.getTransmitterGroupNames());

			logger.info("Activation sent to transmitters.");
		} catch (Exception e) {
			logger.error("Failed to send Activation", e);
			return;
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

	public void handleIdentification() {
		try {
			transmitterManager.sendCallSigns();

			logger.info("Transmitter identifications sent.");
		} catch (Exception e) {
			logger.error("Failed to send transmitter identifications.");
		}
	}

	public TransmitterManager getTransmitterManager() {
		return transmitterManager;
	}
}
