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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.Settings;
import org.dapnet.core.model.Activation;
import org.dapnet.core.model.Call;
import org.dapnet.core.model.CallSign;
import org.dapnet.core.model.News;
import org.dapnet.core.model.Pager;
import org.dapnet.core.model.Rubric;
import org.dapnet.core.transmission.TransmissionSettings.PagingProtocolSettings;

public class SkyperProtocol implements PagerProtocol {
	private static final PagingProtocolSettings settings = Settings.getTransmissionSettings()
			.getPagingProtocolSettings();
	private static final Logger logger = LogManager.getLogger();

	@Override
	public List<Message> createMessagesFromCall(Call call) {
		// Collect all addresses
		List<Integer> addresses = new ArrayList<>();
		try {
			for (CallSign callSign : call.getCallSigns()) {
				for (Pager pager : callSign.getPagers())
					addresses.add(pager.getNumber());
			}
		} catch (Exception e) {
			logger.error("Failed to create messages from call", e);
			return null;
		}

		// Create Messages
		List<Message> messages = new ArrayList<>();

		addresses.forEach((addr) -> messages.add(new Message(call.getText(), addr,
				call.isEmergency() ? Message.MessagePriority.EMERGENCY : Message.MessagePriority.CALL,
				Message.FunctionalBits.ALPHANUM)));

		return messages;
	}

	@Override
	public Message createMessageFromTime(Date date) {
		// Generate timeString in necessary format
		String timeString = new SimpleDateFormat("HHmmss   ddMMyy").format(date);

		// Create Message
		return new Message(timeString, 2504, Message.MessagePriority.TIME, Message.FunctionalBits.NUMERIC);
	}

	@Override
	public Message createMessageFromRubric(Rubric rubric) {
		// Generate Rubric String: Coding adapted from Funkrufmaster
		String rubricString = new String("1");
		rubricString = rubricString + String.valueOf((char) (rubric.getNumber() + 0x1f));
		rubricString = rubricString + String.valueOf((char) (10 + 0x20));

		for (int i = 0; i < rubric.getLabel().length(); ++i) {
			rubricString = rubricString + String.valueOf((char) ((int) rubric.getLabel().charAt(i) + 1));
		}

		// Create Message
		Message message = new Message(rubricString, 4512, Message.MessagePriority.RUBRIC,
				Message.FunctionalBits.ALPHANUM);

		return message;
	}

	@Override
	public Message createMessageFromNews(News news) {
		// Generate News String: Coding adapted from Funkrufmaster
		String newsString = new String("");
		try {
			newsString = newsString + String.valueOf((char) (news.getRubric().getNumber() + 0x1f));
		} catch (Exception e) {
			return null;
		}

		newsString = newsString + String.valueOf((char) (news.getNumber() + 0x20));

		for (int i = 0; i < news.getText().length(); ++i)
			newsString = newsString + String.valueOf((char) ((int) news.getText().charAt(i) + 1));

		// Create Message
		return new Message(newsString, 4520, Message.MessagePriority.NEWS, Message.FunctionalBits.ALPHANUM);
	}

	@Override
	public Message createMessageFromActivation(Activation activation) {
		List<String> activationCode = Arrays.asList(settings.getActivationCode().split(","));
		String activationString = "";

		for (int i = 0; i < activationCode.size(); ++i) {
			List<String> activationSubCode = Arrays.asList(activationCode.get(i).split(" "));

			if (activationSubCode.size() != 3)
				return null;

			int shift = Integer.parseInt(activationSubCode.get(0));
			int mask = Integer.parseInt(activationSubCode.get(1));
			int offset = Integer.parseInt(activationSubCode.get(2));

			int cInt = ((activation.getNumber() >> shift) & mask) + offset;
			char c = (char) cInt;
			activationString = activationString + String.valueOf(c);
		}

		// Create Message
		return new Message(activationString, activation.getNumber(), Message.MessagePriority.ACTIVATION,
				Message.FunctionalBits.ACTIVATION);
	}
}
