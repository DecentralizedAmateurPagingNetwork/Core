package org.dapnet.core.model;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

public class CoreStatistics implements Serializable {

	private static final long serialVersionUID = 2368991802204097911L;

	private AtomicLong calls = new AtomicLong();
	private AtomicLong news = new AtomicLong();

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
