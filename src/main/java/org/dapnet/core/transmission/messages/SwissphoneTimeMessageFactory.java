package org.dapnet.core.transmission.messages;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Swissphone time message factory.
 * 
 * @author Philipp Thiel
 */
class SwissphoneTimeMessageFactory implements PagerMessageFactory<ZonedDateTime> {

	private static final DateTimeFormatter DATE_FORMATTER_SWISSPHONE = DateTimeFormatter
			.ofPattern("'XTIME='HHmmddMMyy");

	@Override
	public Collection<PagerMessage> createMessage(ZonedDateTime payload) {
		Collection<PagerMessage> result = new LinkedList<>();

		// UTC
		final ZonedDateTime utcTime = payload.withZoneSameInstant(ZoneOffset.UTC);
		String s = DATE_FORMATTER_SWISSPHONE.format(utcTime);
		String s2 = s + s;
		PagerMessage msg = new PagerMessage(s2, 200, PagerMessage.MessagePriority.TIME,
				PagerMessage.FunctionalBits.ALPHANUM);
		result.add(msg);

		// Local time
		s = DATE_FORMATTER_SWISSPHONE.format(payload);
		s2 = s + s;
		msg = new PagerMessage(s2, 208, PagerMessage.MessagePriority.TIME, PagerMessage.FunctionalBits.ALPHANUM);
		result.add(msg);

		return Collections.unmodifiableCollection(result);
	}

}
