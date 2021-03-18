package org.dapnet.core.model;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;

/**
 * News list implementation. A news list can hold up to 10 news in slots
 * numbered from 1 to 10. This implementation is thread-safe.
 * 
 * @author Philipp Thiel
 */
public class NewsList implements Serializable, Iterable<News> {

	private static final long serialVersionUID = 1L;
	private News[] slots = new News[10];
	private transient volatile Consumer<News> addHandler;
	private transient volatile Consumer<News> handler;
	private transient volatile Instant lastTrigger;

	/**
	 * Creates a new empty news list.
	 */
	public NewsList() {
	}

	/**
	 * Creates a new news list from the given news collection.
	 * 
	 * @param news News to load.
	 */
	public NewsList(Collection<News> news) {
		for (News n : news) {
			if (n != null) {
				int i = (n.getNumber() - 1) % slots.length;
				slots[i] = n;
			}
		}
	}

	/**
	 * Sets the news list change handler.
	 * 
	 * @param handler Change handler to use.
	 */
	public void setHandler(Consumer<News> handler) {
		this.handler = handler;
	}

	/**
	 * Sets the news list add element handler. This is a workaround for sending
	 * updates to pagers that do not support rubrics and news must be sent to a RIC
	 * instead.
	 * 
	 * @param addHandler Add handler to use.
	 */
	public void setAddHandler(Consumer<News> addHandler) {
		this.addHandler = addHandler;
	}

	/**
	 * Adds a news object to the list.
	 * 
	 * @param news News object to add.
	 */
	public void add(News news) {
		notifyHandler(addHandler, news);

		synchronized (slots) {
			if (news.getNumber() < 1) {
				for (int i = 0; i < slots.length; ++i) {
					if (news != null) {
						news.setNumber(i + 1);
					}

					News next = slots[i];
					slots[i] = news;
					news = next;
				}

				triggerAll();
			} else {
				int idx = (news.getNumber() - 1) % slots.length;
				slots[idx] = news;

				notifyHandler(handler, news);
			}
		}
	}

	/**
	 * Removes the news at the given slot.
	 * 
	 * @param number Slot number (1 to 10).
	 * @return Old news entry if present.
	 * @throws IndexOutOfBoundsException if no news exists at the given slot number.
	 */
	public News remove(int number) {
		if (number < 1 || number > 10) {
			throw new IllegalArgumentException("number must be between 1 and 10.");
		}

		synchronized (slots) {
			News old = slots[number - 1];
			slots[number - 1] = null;

			return old;
		}
	}

	/**
	 * Gets the list size (amount of non-null entries).
	 * 
	 * @return List size
	 */
	public int getSize() {
		int size = 0;

		synchronized (slots) {
			for (int i = 0; i < slots.length; ++i) {
				if (slots[i] != null) {
					++size;
				}
			}
		}

		return size;
	}

	/**
	 * Returns the timestamp when the last trigger operation was run.
	 * 
	 * @return Timestamp
	 */
	public Instant getLastTrigger() {
		return lastTrigger;
	}

	/**
	 * Returns a copy of the news list.
	 * 
	 * @return Copy of the internal list.
	 */
	public Collection<News> getAsList() {
		synchronized (slots) {
			return Arrays.asList(slots);
		}
	}

	/**
	 * Triggers all news by notifying the handler.
	 */
	public void triggerAll() {
		Consumer<News> theHandler = handler;
		if (theHandler == null) {
			return;
		}

		synchronized (slots) {
			for (News n : slots) {
				if (n != null) {
					theHandler.accept(n);
				}
			}
		}

		lastTrigger = Instant.now();
	}

	/**
	 * Removes all expired news.
	 * 
	 * @param now Timepoint to start from.
	 * @param ttl Time to live.
	 */
	public void removeExpired(Instant now, Duration ttl) {
		synchronized (slots) {
			for (int i = 0; i < slots.length; ++i) {
				News n = slots[i];
				if (n != null && now.isAfter(n.getTimestamp().plus(ttl))) {
					slots[i] = null;
				}
			}
		}
	}

	private static void notifyHandler(Consumer<News> handler, News news) {
		if (news != null && handler != null) {
			handler.accept(news);
		}
	}

	@Override
	public Iterator<News> iterator() {
		synchronized (slots) {
			return new CopyIterator(slots);
		}
	}

	private static final class CopyIterator implements Iterator<News> {

		private final News[] slots;
		private int index = 0;

		public CopyIterator(News[] slots) {
			this.slots = Arrays.copyOf(slots, slots.length);
		}

		@Override
		public boolean hasNext() {
			return index < slots.length;
		}

		@Override
		public News next() {
			return slots[index++];
		}

	}

}
