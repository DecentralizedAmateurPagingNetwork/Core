package org.dapnet.core.transmission.messages;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

import org.dapnet.core.transmission.messages.PagerMessage.ContentType;
import org.dapnet.core.transmission.messages.PagerMessage.Priority;
import org.dapnet.core.transmission.messages.PagerMessage.SubAddress;

/**
 * Skyper time message factory.
 * 
 * @author Philipp Thiel
 */
class SkyperTimeMessageFactory implements PagerMessageFactory<ZonedDateTime> {

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("HHmmss   ddMMyy");
	private static final int TIME_ADDRESS = 2504;
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

		PagerMessage message = new PagerMessage(Priority.TIME, TIME_ADDRESS, SubAddress.ADDR_A, ContentType.NUMERIC,
				timeString);

		return List.of(message);
	}

}
