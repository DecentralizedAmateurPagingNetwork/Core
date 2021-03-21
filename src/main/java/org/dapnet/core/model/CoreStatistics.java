package org.dapnet.core.model;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

public class CoreStatistics implements Serializable {

	private static final long serialVersionUID = 1L;

	private AtomicLong calls = new AtomicLong();
	private AtomicLong news = new AtomicLong();

	public CoreStatistics() {
	}

	public CoreStatistics(CoreStatistics other) {
		if (other == null) {
			throw new NullPointerException("Other object must not be null.");
		}

		calls = new AtomicLong(other.calls.get());
		news = new AtomicLong(other.news.get());
	}

	public long getCalls() {
		return calls.get();
	}

	public long incrementCalls() {
		return calls.incrementAndGet();
	}

	public long getNews() {
		return news.get();
	}

	public long incrementNews() {
		return calls.incrementAndGet();
	}

}
