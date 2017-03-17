package org.dapnet.core.model;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;

public class NewsList implements Serializable, Iterable<News> {

	private static final long serialVersionUID = 8878795787181440875L;
	private LinkedList<News> slots = new LinkedList<>();

	public void add(News news) {
		if (news.getNumber() == 0) {
			slots.addFirst(news);
			if (slots.size() > 10) {
				slots.removeLast();
			}

			updateNumbers();
		} else {
			slots.set(news.getNumber() - 1, news);
		}
	}

	private void updateNumbers() {
		int num = 1;
		for (News n : slots) {
			n.setNumber(num++);
		}
	}

	@Override
	public Iterator<News> iterator() {
		return slots.iterator();
	}

}
