package org.dapnet.core.transmission.messages;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

/**
 * Skyper time message factory.
 * 
 * @author Philipp Thiel
 */
class SkyperTimeMessageFactory implements PagerMessageFactory<ZonedDateTime> {

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("HHmmss   ddMMyy");
	private final boolean uselocalTime;

	/**
	 * Constructs a new Skyper time message factory.
	 * 
	 * @param uselocalTime Use local time instead of UTC
	 */
	public SkyperTimeMessageFactory(boolean uselocalTime) {
		this.uselocalTime = uselocalTime;
	}

	@Override
	public Collection<PagerMessage> createMessage(ZonedDateTime payload) {
		String timeString = null;

		if (uselocalTime) {
			timeString = DATE_FORMATTER.format(payload);
		} else {
			ZonedDateTime utcTime = payload.withZoneSameInstant(ZoneOffset.UTC);
			timeString = DATE_FORMATTER.format(utcTime);
		}

		PagerMessage message = new PagerMessage(timeString, 2504, PagerMessage.MessagePriority.TIME,
				PagerMessage.FunctionalBits.NUMERIC);

		return List.of(message);
	}

}
