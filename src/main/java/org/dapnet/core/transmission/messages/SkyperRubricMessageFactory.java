package org.dapnet.core.transmission.messages;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.dapnet.core.model.Rubric;
import org.dapnet.core.transmission.messages.PagerMessage.ContentType;
import org.dapnet.core.transmission.messages.PagerMessage.Priority;
import org.dapnet.core.transmission.messages.PagerMessage.SubAddress;

/**
 * Skyper rubric message factory.
 * 
 * @author Philipp Thiel
 */
class SkyperRubricMessageFactory implements PagerMessageFactory<Rubric> {

	private static final int RUBRIC_ADDRESS = 4512;
	private final Function<String, String> encoder;

	/**
	 * Constructs a new Skyper rubric message factory.
	 * 
	 * @param encoder String encoder to use
	 */
	public SkyperRubricMessageFactory(Function<String, String> encoder) {
		this.encoder = Objects.requireNonNull(encoder, "Encoder must not be null.");
	}

	@Override
	public Collection<PagerMessage> createMessage(Rubric payload) {
		// Rubric message creation adapted from Funkrufmaster
		String label = encoder.apply(payload.getLabel());
		StringBuilder sb = new StringBuilder();
		sb.append("1");
		sb.append(String.valueOf((char) (payload.getNumber() + 0x1f)));
		sb.append(String.valueOf((char) (10 + 0x20)));

		for (int i = 0; i < label.length(); ++i) {
			sb.append(String.valueOf((char) ((int) label.charAt(i) + 1)));
		}

		PagerMessage message = new PagerMessage(Priority.RUBRIC, RUBRIC_ADDRESS, SubAddress.ADDR_D,
				ContentType.ALPHANUMERIC, sb.toString());

		return List.of(message);
	}

}
