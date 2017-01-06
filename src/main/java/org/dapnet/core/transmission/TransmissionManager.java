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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.model.Activation;
import org.dapnet.core.model.Call;
import org.dapnet.core.model.News;
import org.dapnet.core.model.Rubric;

import java.util.Date;
import java.util.List;

public class TransmissionManager {
	private static final Logger logger = LogManager.getLogger(TransmissionManager.class.getName());
	private final PagerProtocol protocol = new SkyperProtocol();
	private final TransmitterDeviceManager deviceManager = new TransmitterDeviceManager();

	public void handleTime(Date date) {
		try {
			Message message = protocol.createMessageFromTime(date);
			// Possibility to implement TimeZones by handling here
			// TransmitterGroups
			// Now sending Time to all connected Devices
			deviceManager.getTransmitterDevices().forEach((e) -> e.sendMessage(message));

			logger.info("Time sent using {} Transmitter", deviceManager.getTransmitterDevices().size());
		} catch (Exception e) {
			logger.error("Failed to send Time", e);
		}
	}

	public void handleNews(News news) {
		try {
			Message message = protocol.createMessageFromNews(news);
			List<TransmitterDevice> transmitterDevices = deviceManager
					.getTransmitterDevices(news.getRubric().getTransmitterGroups());

			transmitterDevices.forEach((e) -> e.sendMessage(message));

			logger.info("News sent using {} Transmitter", transmitterDevices.size());
		} catch (Exception e) {
			logger.error("Failed to send News", e);
		}
	}

	public void handleRubric(Rubric rubric) {
		try {
			Message message = protocol.createMessageFromRubric(rubric);
			List<TransmitterDevice> transmitterDevices = deviceManager
					.getTransmitterDevices(rubric.getTransmitterGroups());

			transmitterDevices.forEach((e) -> e.sendMessage(message));

			logger.info("Rubric sent using {} Transmitter", transmitterDevices.size());
		} catch (Exception e) {
			logger.error("Failed to send Rubric", e);
		}
	}

	public void handleCall(Call call) {
		try {
			List<Message> messages = protocol.createMessagesFromCall(call);
			List<TransmitterDevice> transmitterDevices = deviceManager
					.getTransmitterDevices(call.getTransmitterGroups());

			transmitterDevices.forEach((e) -> e.sendMessages(messages));

			logger.info("Call sent to {} CallSigns, to {} Pager, using {} TransmitterGroups and {} Transmitter",
					call.getCallSigns().size(), messages.size(), call.getTransmitterGroupNames().size(),
					transmitterDevices.size());
		} catch (Exception e) {
			logger.error("Failed to send Call", e);
		}
	}

	public void handleActivation(Activation activation) {
		try {
			Message message = protocol.createMessageFromActivation(activation);
			List<TransmitterDevice> transmitterDevices = deviceManager
					.getTransmitterDevices(activation.getTransmitterGroups());

			transmitterDevices.forEach((e) -> e.sendMessage(message));

			logger.info("Activation sent using {} Transmitter", transmitterDevices.size());
		} catch (Exception e) {
			logger.error("Failed to send Activation", e);
		}
	}

	public TransmitterDeviceManager getTransmitterDeviceManager() {
		return deviceManager;
	}
}
