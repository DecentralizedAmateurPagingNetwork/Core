package org.dapnet.core.transmission.messages;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;

import org.dapnet.core.model.CoreRepository;
import org.dapnet.core.model.News;
import org.dapnet.core.model.Rubric;

/**
 * A news message factory where the news are send to a RIC defined by the
 * rubric.
 * 
 * @author Philipp Thiel
 */
class RicNewsMessageFactory implements PagerMessageFactory<News> {

	private final CoreRepository repository;

	public RicNewsMessageFactory(CoreRepository repository) {
		this.repository = Objects.requireNonNull(repository, "Repository must not be null.");
	}

	@Override
	public Collection<PagerMessage> createMessage(News payload) {
		Lock lock = repository.getLock().readLock();
		lock.lock();

		try {
			Rubric rubric = repository.getRubrics().get(payload.getRubricName());
			if (rubric != null) {
				PagerMessage message = new PagerMessage(payload.getText(), rubric.getAddress(),
						PagerMessage.MessagePriority.NEWS, PagerMessage.FunctionalBits.ALPHANUM);

				return List.of(message);
			} else {
				return null;
			}
		} finally {
			lock.unlock();
		}
	}

}
