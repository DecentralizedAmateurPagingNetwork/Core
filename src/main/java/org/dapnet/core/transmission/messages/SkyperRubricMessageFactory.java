package org.dapnet.core.transmission.messages;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.dapnet.core.model.Rubric;

/**
 * Skyper rubric message factory.
 * 
 * @author Philipp Thiel
 */
class SkyperRubricMessageFactory implements PagerMessageFactory<Rubric> {

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

		PagerMessage message = new PagerMessage(sb.toString(), 4512, PagerMessage.MessagePriority.RUBRIC,
				PagerMessage.FunctionalBits.ALPHANUM);

		return List.of(message);
	}

}
