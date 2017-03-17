package org.dapnet.core.model;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Consumer;

public class NewsList implements Serializable, Iterable<News> {

	private static final long serialVersionUID = 8878795787181440875L;
	private LinkedList<News> slots = new LinkedList<>();
	private transient volatile Consumer<News> handler;

	public void setHandler(Consumer<News> handler) {
		this.handler = handler;
	}

	public synchronized void add(News news) {
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

	public synchronized News remove(int number) {
		if (number < 1 || number > 10) {
			throw new IllegalArgumentException("number must be between 1 and 10.");
		}

		News old = slots.remove(number - 1);
		if (old != null) {
			updateNumbers();
		}

		return old;
	}

	public synchronized int getSize() {
		return slots.size();
	}

	public synchronized Collection<News> getList() {
		return Collections.unmodifiableCollection(slots);
	}

	public synchronized void removeExpired(Duration ttl) {
		final Instant now = Instant.now();
		boolean changed = false;

		Iterator<News> it = slots.iterator();
		while (it.hasNext()) {
			News n = it.next();
			if (n.getTimestamp().plus(ttl).isAfter(now)) {
				it.remove();
				changed = true;
			}
		}

		if (changed) {
			updateNumbers();
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
		Consumer<News> theHandler = handler;
		if (theHandler != null) {
			theHandler.accept(news);
		}
	}

	@Override
	public Iterator<News> iterator() {
		return slots.iterator();
	}

}
