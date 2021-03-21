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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.Settings;
import org.dapnet.core.model.Activation;
import org.dapnet.core.model.Call;
import org.dapnet.core.model.CallSign;
import org.dapnet.core.model.CoreRepository;
import org.dapnet.core.model.ModelRepository;
import org.dapnet.core.model.News;
import org.dapnet.core.model.Pager;
import org.dapnet.core.model.Rubric;
import org.dapnet.core.transmission.PagerMessage.FunctionalBits;
import org.dapnet.core.transmission.PagerMessage.MessagePriority;
import org.dapnet.core.transmission.TransmissionSettings.PagingProtocolSettings;

public class SkyperProtocol implements PagerProtocol {
	private static final PagingProtocolSettings settings = Settings.getTransmissionSettings()
			.getPagingProtocolSettings();
	private static final Logger logger = LogManager.getLogger();
	private static final Pattern NUMERIC_PATTERN = Pattern.compile("[-Uu\\d\\(\\) ]+");
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("HHmmss   ddMMyy");
	private static final DateTimeFormatter DATE_FORMATTER_SWISSPHONE = DateTimeFormatter
			.ofPattern("'XTIME='HHmmddMMyy");
	private static final DateTimeFormatter DATE_FORMATTER_ALPHAPOC = DateTimeFormatter
			.ofPattern("'YYYYMMDDHHMMSS'yyMMddHHmm'00'");
	private static final Charset PAGER_CHARSET = new DE_ASCII7();

	private final CoreRepository repository;

	public SkyperProtocol(CoreRepository stateManager) {
		this.repository = Objects.requireNonNull(stateManager, "State manager must not be null.");
	}

	@Override
	public Collection<PagerMessage> createMessagesFromCall(Call call) {
		MessagePriority priority = call.isEmergency() ? MessagePriority.EMERGENCY : MessagePriority.CALL;
		Instant now = Instant.now();

		Lock lock = repository.getLock().readLock();
		lock.lock();

		try {
			// Test if message is numeric
			Matcher m = NUMERIC_PATTERN.matcher(call.getText());
			boolean numeric = m.matches();

			final ModelRepository<CallSign> callsigns = repository.getCallSigns();

			Collection<PagerMessage> messages = new LinkedList<>();
			for (String name : call.getCallSignNames()) {
				CallSign callsign = callsigns.get(name);
				if (callsign == null) {
					logger.error("Callsign does not exist: {}", name);
					continue;
				}

				FunctionalBits mode;
				String text;
				if (!callsign.isNumeric()) {
					// Support for alphanumeric messages -> create ALPHANUM message
					mode = FunctionalBits.ALPHANUM;
					text = encodeString(call.getText());
				} else if (numeric) {
					// No support for alphanumeric messages but text is numeric -> create NUMERIC
					// message
					mode = FunctionalBits.NUMERIC;
					text = call.getText().toUpperCase();
				} else {
					// No support for alphanumeric messages and non-numeric message -> skip
					logger.warn("Callsign {} does not support alphanumeric messages.", callsign.getName());
					continue;
				}

				for (Pager pager : callsign.getPagers()) {
					messages.add(new PagerMessage(now, text, pager.getNumber(), priority, mode));
				}
			}

			return messages;
		} catch (Exception ex) {
			logger.error("Failed to create messages from call.", ex);
			return null;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public PagerMessage createMessageFromTime(LocalDateTime time) {
		return new PagerMessage(DATE_FORMATTER.format(time), 2504, PagerMessage.MessagePriority.TIME,
				PagerMessage.FunctionalBits.NUMERIC);
	}

	@Override
	public PagerMessage createMessageFromTimeSwissphone(LocalDateTime time) {
		String s = DATE_FORMATTER_SWISSPHONE.format(time);
		String s2 = s + s;
		return new PagerMessage(s2, 200, PagerMessage.MessagePriority.TIME, PagerMessage.FunctionalBits.ALPHANUM);
	}

	@Override
	public PagerMessage createMessageFromLocalTimeSwissphone(LocalDateTime localTime) {
		String s = DATE_FORMATTER_SWISSPHONE.format(localTime);
		String s2 = s + s;
		return new PagerMessage(s2, 208, PagerMessage.MessagePriority.TIME, PagerMessage.FunctionalBits.ALPHANUM);
	}

	@Override
	public PagerMessage createMessageFromTimeAlphaPoc(LocalDateTime time) {
		String s = DATE_FORMATTER_ALPHAPOC.format(time);
		return new PagerMessage(s, 216, PagerMessage.MessagePriority.TIME, PagerMessage.FunctionalBits.ALPHANUM);
	}

	@Override
	public PagerMessage createMessageFromLocalTimeAlphaPoc(LocalDateTime time) {
		String s = DATE_FORMATTER_ALPHAPOC.format(time);
		return new PagerMessage(s, 224, PagerMessage.MessagePriority.TIME, PagerMessage.FunctionalBits.ALPHANUM);
	}

	@Override
	public PagerMessage createMessageFromRubric(Rubric rubric) {
		// Generate Rubric String: Coding adapted from Funkrufmaster
		String label = encodeString(rubric.getLabel());
		StringBuilder sb = new StringBuilder();
		sb.append("1");
		sb.append(String.valueOf((char) (rubric.getNumber() + 0x1f)));
		sb.append(String.valueOf((char) (10 + 0x20)));

		for (int i = 0; i < label.length(); ++i) {
			sb.append(String.valueOf((char) ((int) label.charAt(i) + 1)));
		}

		return new PagerMessage(sb.toString(), 4512, PagerMessage.MessagePriority.RUBRIC,
				PagerMessage.FunctionalBits.ALPHANUM);
	}

	@Override
	public PagerMessage createMessageFromNews(News news) {
		// Generate News String: Coding adapted from Funkrufmaster
		String text = encodeString(news.getText());
		StringBuilder sb = new StringBuilder();

		Lock lock = repository.getLock().readLock();
		lock.lock();

		try {
			Rubric rubric = repository.getRubrics().get(news.getRubricName());
			if (rubric != null) {
				sb.append(String.valueOf((char) (rubric.getNumber() + 0x1f)));
			} else {
				logger.error("Could not create message, rubric not found: {}", news.getRubricName());
				return null;
			}
		} catch (Exception e) {
			return null;
		} finally {
			lock.unlock();
		}

		sb.append(String.valueOf((char) (news.getNumber() + 0x20)));

		for (int i = 0; i < text.length(); ++i) {
			sb.append(String.valueOf((char) ((int) text.charAt(i) + 1)));
		}

		// Create Message
		return new PagerMessage(sb.toString(), 4520, PagerMessage.MessagePriority.NEWS,
				PagerMessage.FunctionalBits.ALPHANUM);
	}

	@Override
	public PagerMessage createMessageFromNewsAsCall(News news) {
		Lock lock = repository.getLock().readLock();
		lock.lock();

		try {
			Rubric rubric = repository.getRubrics().get(news.getRubricName());
			if (rubric != null) {
				return new PagerMessage(news.getText(), rubric.getAddress(), PagerMessage.MessagePriority.CALL,
						PagerMessage.FunctionalBits.ALPHANUM);
			} else {
				logger.error("Could not create message, rubric not found: {}", news.getRubricName());
				return null;
			}
		} catch (Exception ex) {
			logger.error("Failed to create message from news as call.", ex);
			return null;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public PagerMessage createMessageFromActivation(Activation activation) {
		List<String> activationCode = Arrays.asList(settings.getActivationCode().split(","));
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < activationCode.size(); ++i) {
			List<String> activationSubCode = Arrays.asList(activationCode.get(i).split(" "));
			if (activationSubCode.size() != 3) {
				return null;
			}

			int shift = Integer.parseInt(activationSubCode.get(0));
			int mask = Integer.parseInt(activationSubCode.get(1));
			int offset = Integer.parseInt(activationSubCode.get(2));

			int cInt = ((activation.getNumber() >> shift) & mask) + offset;
			char c = (char) cInt;
			sb.append(String.valueOf(c));
		}

		return new PagerMessage(sb.toString(), activation.getNumber(), PagerMessage.MessagePriority.ACTIVATION,
				PagerMessage.FunctionalBits.ACTIVATION);
	}

	private static String encodeString(String input) {
		if (input != null) {
			byte[] encoded = input.getBytes(PAGER_CHARSET);
			return new String(encoded, StandardCharsets.US_ASCII);
		} else {
			return null;
		}
	}
}
