package org.dapnet.core.model;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Consumer;

/**
 * News list implementation. A news list can hold up to 10 news in slots
 * numbered from 1 to 10. This implementation is thread-safe.
 * 
 * @author Philipp Thiel
 */
public class NewsList implements Serializable, Iterable<News> {

	private static final long serialVersionUID = 8878795787181440875L;
	private final Object lockObj = new Object();
	private final LinkedList<News> slots;
	private transient volatile Consumer<News> handler;

	/**
	 * Creates a new empty news list.
	 */
	public NewsList() {
		this(new LinkedList<>());
	}

	/**
	 * Creates a new list backed by the given list.
	 * 
	 * @param slots
	 *            List to use
	 */
	public NewsList(LinkedList<News> slots) {
		if (slots == null) {
			throw new NullPointerException("slots cannot be null.");
		}

		this.slots = slots;
	}

	/**
	 * Sets the news list change handler.
	 * 
	 * @param handler
	 *            Change handler
	 */
	public void setHandler(Consumer<News> handler) {
		this.handler = handler;
	}

	/**
	 * Adds a news object to the list.
	 * 
	 * @param news
	 *            News object to add.
	 */
	public void add(News news) {
		synchronized (lockObj) {
			if (news.getNumber() < 1 || slots.size() == 0) {
				slots.addFirst(news);
				if (slots.size() > 10) {
					slots.removeLast();
				}

				updateNumbers();
			} else {
				int idx = Integer.min(news.getNumber() - 1, slots.size());
				slots.set(idx, news);
				notifyHandler(news);
			}
		}
	}

	/**
	 * Removes the news at the given slot.
	 * 
	 * @param number
	 *            Slot number (1 to 10).
	 * @return Old news entry if present.
	 * @throws IndexOutOfBoundsException
	 *             if no news exists at the given slot number.
	 */
	public News remove(int number) {
		if (number < 1 || number > 10) {
			throw new IllegalArgumentException("number must be between 1 and 10.");
		}

		synchronized (lockObj) {
			News old = slots.remove(number - 1);
			if (old != null) {
				updateNumbers();
			}

			return old;
		}
	}

	/**
	 * Returns the list size.
	 * 
	 * @return List size
	 */
	public int getSize() {
		synchronized (lockObj) {
			return slots.size();
		}
	}

	/**
	 * Returns a copy of the news list.
	 * 
	 * @return Copy of the internal list.
	 */
	public Collection<News> getAsList() {
		synchronized (lockObj) {
			return new ArrayList<>(slots);
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

		synchronized (lockObj) {
			for (News n : slots) {
				theHandler.accept(n);
			}
		}
	}

	/**
	 * Removes all expired news.
	 * 
	 * @param now
	 *            Timepoint to start from.
	 * @param ttl
	 *            Time to live.
	 */
	public void removeExpired(Instant now, Duration ttl) {
		boolean changed = false;

		synchronized (lockObj) {
			Iterator<News> it = slots.iterator();
			while (it.hasNext()) {
				News n = it.next();
				if (now.isAfter(n.getTimestamp().plus(ttl))) {
					it.remove();
					changed = true;
				}
			}

			if (changed) {
				updateNumbers();
			}
		}
	}

	private void updateNumbers() {
		int num = 1;
		for (News n : slots) {
			if (n.getNumber() != num) {
				n.setNumber(num);
				notifyHandler(n);
			}

			++num;
		}
	}

	private void notifyHandler(News news) {
		System.out.println("DEBUG notifyHandler");
		Consumer<News> theHandler = handler;
		if (theHandler != null) {
			theHandler.accept(news);
		}
	}

	@Override
	public Iterator<News> iterator() {
		synchronized (lockObj) {
			return new CopyIterator(slots);
		}
	}

	private static final class CopyIterator implements Iterator<News> {

		private final Collection<News> data;
		private final Iterator<News> iterator;

		public CopyIterator(Collection<News> copyFrom) {
			data = new ArrayList<>(copyFrom);
			iterator = data.iterator();
		}

		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public News next() {
			return iterator.next();
		}

	}

}
