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

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.model.Activation;
import org.dapnet.core.model.Call;
import org.dapnet.core.model.News;
import org.dapnet.core.model.Rubric;

public class TransmissionManager {
	private static final Logger logger = LogManager.getLogger();
	private final PagerProtocol protocol = new SkyperProtocol();
	private final TransmitterManager transmitterManager = new TransmitterManager();

	public void handleTime(Date date) {
		try {
			Message message = protocol.createMessageFromTime(date);
			transmitterManager.sendMessage(message);

			logger.info("Time sent to transmitters.");
		} catch (Exception e) {
			logger.error("Failed to send Time", e);
		}
	}

	public void handleNews(News news) {
		try {
			Message message = protocol.createMessageFromNews(news);
			transmitterManager.sendMessage(message, news.getRubric().getTransmitterGroups());

			logger.info("News sent to transmitters.");
		} catch (Exception e) {
			logger.error("Failed to send News", e);
		}
	}

	public void handleNewsAsCall(News news) {
		try {
			Message message = protocol.createMessageFromNewsAsCall(news);
			transmitterManager.sendMessage(message, news.getRubric().getTransmitterGroups());

			logger.info("News sent to transmitters as call.");
		} catch (Exception ex) {
			logger.error("Failed to send News as call", ex);
		}
	}

	public void handleRubric(Rubric rubric) {
		try {
			Message message = protocol.createMessageFromRubric(rubric);
			transmitterManager.sendMessage(message, rubric.getTransmitterGroups());

			logger.info("Rubric sent to transmitters.");
		} catch (Exception e) {
			logger.error("Failed to send Rubric", e);
		}
	}

	public void handleCall(Call call) {
		try {
			List<Message> messages = protocol.createMessagesFromCall(call);
			transmitterManager.sendMessages(messages, call.getTransmitterGroups());

			logger.info("Call sent to {} CallSigns, to {} Pagers, using {} TransmitterGroups.",
					call.getCallSigns().size(), messages.size(), call.getTransmitterGroupNames().size());
		} catch (Exception e) {
			logger.error("Failed to send Call", e);
		}
	}

	public void handleActivation(Activation activation) {
		try {
			Message message = protocol.createMessageFromActivation(activation);
			transmitterManager.sendMessage(message, activation.getTransmitterGroups());

			logger.info("Activation sent to transmitters.");
		} catch (Exception e) {
			logger.error("Failed to send Activation", e);
		}
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
