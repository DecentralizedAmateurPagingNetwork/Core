package org.dapnet.core.transmission.messages;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Alphapoc time message factory.
 * 
 * @author Philipp Thiel
 */
class AlphapocTimeMessageFactory implements PagerMessageFactory<ZonedDateTime> {

	private static final DateTimeFormatter DATE_FORMATTER_ALPHAPOC = DateTimeFormatter
			.ofPattern("'YYYYMMDDHHMMSS'yyMMddHHmm'00'");

	@Override
	public Collection<PagerMessage> createMessage(ZonedDateTime payload) {
		final Collection<PagerMessage> result = new LinkedList<>();

		// UTC
		final ZonedDateTime utcTime = payload.withZoneSameInstant(ZoneOffset.UTC);
		String s = DATE_FORMATTER_ALPHAPOC.format(utcTime);
		PagerMessage msg = new PagerMessage(s, 216, PagerMessage.MessagePriority.TIME,
				PagerMessage.FunctionalBits.ALPHANUM);
		result.add(msg);

		// Local time
		s = DATE_FORMATTER_ALPHAPOC.format(payload);
		msg = new PagerMessage(s, 224, PagerMessage.MessagePriority.TIME, PagerMessage.FunctionalBits.ALPHANUM);
		result.add(msg);

		return Collections.unmodifiableCollection(result);
	}

}
