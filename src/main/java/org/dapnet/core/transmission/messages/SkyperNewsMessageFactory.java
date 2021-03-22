package org.dapnet.core.transmission.messages;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;

import org.dapnet.core.model.CoreRepository;
import org.dapnet.core.model.News;
import org.dapnet.core.model.Rubric;
import org.dapnet.core.transmission.messages.PagerMessage.ContentType;
import org.dapnet.core.transmission.messages.PagerMessage.Priority;
import org.dapnet.core.transmission.messages.PagerMessage.SubAddress;

/**
 * Skyper news message factory.
 * 
 * @author Philipp Thiel
 */
class SkyperNewsMessageFactory implements PagerMessageFactory<News> {

	private static final int NEWS_ADDRESS = 4520;
	private final CoreRepository repository;
	private final Function<String, String> encoder;

	/**
	 * Constructs a new Skyper news message factory.
	 * 
	 * @param repository Repository to use
	 * @param encoder    String encoder to use
	 */
	public SkyperNewsMessageFactory(CoreRepository repository, Function<String, String> encoder) {
		this.repository = Objects.requireNonNull(repository, "Repository must not be null.");
		this.encoder = Objects.requireNonNull(encoder, "Encoder must not be null.");
	}

	@Override
	public Collection<PagerMessage> createMessage(News payload) {
		// News message creation adapted from Funkrufmaster
		String text = encoder.apply(payload.getText());
		StringBuilder sb = new StringBuilder();

		Lock lock = repository.getLock().readLock();
		lock.lock();

		try {
			Rubric rubric = repository.getRubrics().get(payload.getRubricName());
			if (rubric != null) {
				sb.append(String.valueOf((char) (rubric.getNumber() + 0x1f)));
			} else {
				return null;
			}
		} finally {
			lock.unlock();
		}

		sb.append(String.valueOf((char) (payload.getNumber() + 0x20)));

		for (int i = 0; i < text.length(); ++i) {
			sb.append(String.valueOf((char) ((int) text.charAt(i) + 1)));
		}

		PagerMessage message = new PagerMessage(Priority.NEWS, NEWS_ADDRESS, SubAddress.ADDR_D,
				ContentType.ALPHANUMERIC, sb.toString());

		return List.of(message);
	}

}
