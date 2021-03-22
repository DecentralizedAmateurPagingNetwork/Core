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
 * A news message factory where the news are send to a RIC defined by the
 * rubric.
 * 
 * @author Philipp Thiel
 */
class RicNewsMessageFactory implements PagerMessageFactory<News> {

	private final CoreRepository repository;
	private final Function<String, String> encoder;

	public RicNewsMessageFactory(CoreRepository repository, Function<String, String> encoder) {
		this.repository = Objects.requireNonNull(repository, "Repository must not be null.");
		this.encoder = Objects.requireNonNull(encoder, "Encoder must not be null.");
	}

	@Override
	public Collection<PagerMessage> createMessage(News payload) {
		Lock lock = repository.getLock().readLock();
		lock.lock();

		try {
			Rubric rubric = repository.getRubrics().get(payload.getRubricName());
			if (rubric != null) {
				String content = encoder.apply(payload.getText());
				PagerMessage message = new PagerMessage(Priority.NEWS, rubric.getAddress(), SubAddress.ADDR_D,
						ContentType.ALPHANUMERIC, content);

				return List.of(message);
			} else {
				return null;
			}
		} finally {
			lock.unlock();
		}
	}

}
