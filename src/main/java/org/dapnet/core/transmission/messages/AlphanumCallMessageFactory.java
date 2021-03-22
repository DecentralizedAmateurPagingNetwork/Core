package org.dapnet.core.transmission.messages;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.model.Call;
import org.dapnet.core.model.CallSign;
import org.dapnet.core.model.CoreRepository;
import org.dapnet.core.model.ModelRepository;
import org.dapnet.core.model.Pager;
import org.dapnet.core.transmission.messages.PagerMessage.ContentType;
import org.dapnet.core.transmission.messages.PagerMessage.Priority;
import org.dapnet.core.transmission.messages.PagerMessage.SubAddress;

/**
 * Generic alphanumeric call message factory.
 * 
 * @author Philipp Thiel
 */
class AlphanumCallMessageFactory implements PagerMessageFactory<Call> {

	private static final Logger logger = LogManager.getLogger();
	private final CoreRepository repository;
	private final Function<String, String> encoder;
	private final Pager.Type pagerType;
	private final SubAddress subAddress;

	/**
	 * Constructs a new message factory instance.
	 * 
	 * @param repository Repository to use
	 * @param encoder    String encoder to use
	 * @param pagerType  Pager type to check for (filters message creation)
	 * @param subAddress Sub-address to use for messages
	 */
	public AlphanumCallMessageFactory(CoreRepository repository, Function<String, String> encoder, Pager.Type pagerType,
			SubAddress subAddress) {
		this.repository = Objects.requireNonNull(repository, "Repository must not be null.");
		this.encoder = Objects.requireNonNull(encoder, "Encoder must not be null.");
		this.pagerType = Objects.requireNonNull(pagerType, "Pager type must not be null.");
		this.subAddress = Objects.requireNonNull(subAddress, "Sub-Address must not be null.");
	}

	@Override
	public Collection<PagerMessage> createMessage(Call payload) {
		final Priority priority = payload.isEmergency() ? Priority.EMERGENCY : Priority.CALL;
		final Instant now = Instant.now();
		final String text = encoder.apply(payload.getText());

		final Collection<PagerMessage> messages = new LinkedList<>();
		final Lock lock = repository.getLock().readLock();
		lock.lock();

		try {
			final ModelRepository<CallSign> callsigns = repository.getCallSigns();

			for (String name : payload.getCallSignNames()) {
				final CallSign callsign = callsigns.get(name);
				if (callsign == null) {
					logger.error("Callsign does not exist: {}", name);
					continue;
				}

				for (Pager pager : callsign.getPagers()) {
					if (pager.getType() == pagerType) {
						PagerMessage message = new PagerMessage(now, priority, pager.getNumber(), subAddress,
								ContentType.ALPHANUMERIC, text);
						messages.add(message);
					}
				}
			}
		} finally {
			lock.unlock();
		}

		return Collections.unmodifiableCollection(messages);
	}

}
