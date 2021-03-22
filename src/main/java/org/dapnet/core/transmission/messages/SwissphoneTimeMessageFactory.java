package org.dapnet.core.transmission.messages;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.dapnet.core.transmission.messages.PagerMessage.ContentType;
import org.dapnet.core.transmission.messages.PagerMessage.Priority;
import org.dapnet.core.transmission.messages.PagerMessage.SubAddress;

/**
 * Swissphone time message factory.
 * 
 * @author Philipp Thiel
 */
class SwissphoneTimeMessageFactory implements PagerMessageFactory<ZonedDateTime> {

	private static final DateTimeFormatter DATE_FORMATTER_SWISSPHONE = DateTimeFormatter
			.ofPattern("'XTIME='HHmmddMMyy");
	private static final int UTC_ADDRESS = 200;
	private static final int LOCAL_ADDRESS = 208;

	@Override
	public Collection<PagerMessage> createMessage(ZonedDateTime payload) {
		Collection<PagerMessage> result = new LinkedList<>();

		// UTC
		final ZonedDateTime utcTime = payload.withZoneSameInstant(ZoneOffset.UTC);
		String timeStr = DATE_FORMATTER_SWISSPHONE.format(utcTime);
		timeStr = timeStr + timeStr;
		PagerMessage msg = new PagerMessage(Priority.TIME, UTC_ADDRESS, SubAddress.ADDR_D, ContentType.ALPHANUMERIC,
				timeStr);
		result.add(msg);

		// Local time
		timeStr = DATE_FORMATTER_SWISSPHONE.format(payload);
		timeStr = timeStr + timeStr;
		msg = new PagerMessage(Priority.TIME, LOCAL_ADDRESS, SubAddress.ADDR_D, ContentType.ALPHANUMERIC, timeStr);
		result.add(msg);

		return Collections.unmodifiableCollection(result);
	}

}
