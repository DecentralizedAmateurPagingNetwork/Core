package org.dapnet.core.transmission.messages;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.dapnet.core.model.Activation;
import org.dapnet.core.transmission.messages.PagerMessage.ContentType;
import org.dapnet.core.transmission.messages.PagerMessage.Priority;
import org.dapnet.core.transmission.messages.PagerMessage.SubAddress;

/**
 * Skyper activation message factory.
 * 
 * @author Philipp Thiel
 */
class SkyperActivationMessageFactory implements PagerMessageFactory<Activation> {

	private final String[] activationCode;

	/**
	 * Constructs a new Skyper activation message factory.
	 * 
	 * @param activationCode Activation code to use
	 */
	public SkyperActivationMessageFactory(String[] activationCode) {
		this.activationCode = Objects.requireNonNull(activationCode, "Activation code must not be null.");
	}

	@Override
	public Collection<PagerMessage> createMessage(Activation payload) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < activationCode.length; ++i) {
			String[] activationSubCode = activationCode[i].split(" ");
			if (activationSubCode.length != 3) {
				return null;
			}

			int shift = Integer.parseInt(activationSubCode[0]);
			int mask = Integer.parseInt(activationSubCode[1]);
			int offset = Integer.parseInt(activationSubCode[2]);

			int cInt = ((payload.getNumber() >> shift) & mask) + offset;
			char c = (char) cInt;
			sb.append(String.valueOf(c));
		}

		PagerMessage message = new PagerMessage(Priority.ACTIVATION, payload.getNumber(), SubAddress.ADDR_C,
				ContentType.ALPHANUMERIC, sb.toString());

		return List.of(message);
	}

}
