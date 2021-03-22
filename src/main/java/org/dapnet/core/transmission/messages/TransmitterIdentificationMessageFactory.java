package org.dapnet.core.transmission.messages;

import java.util.Collection;
import java.util.List;

import org.dapnet.core.transmission.messages.PagerMessage.ContentType;
import org.dapnet.core.transmission.messages.PagerMessage.Priority;
import org.dapnet.core.transmission.messages.PagerMessage.SubAddress;

/**
 * Pager message factory for transmitter identification messages.
 * 
 * @author Philipp Thiel
 */
class TransmitterIdentificationMessageFactory implements PagerMessageFactory<TransmitterIdentification> {

	@Override
	public Collection<PagerMessage> createMessage(TransmitterIdentification payload) {
		PagerMessage message = new PagerMessage(Priority.CALL, payload.getAddress(), SubAddress.ADDR_D,
				ContentType.ALPHANUMERIC, payload.getIdentification());

		return List.of(message);
	}

}
