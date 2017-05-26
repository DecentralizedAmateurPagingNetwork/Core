/*
 * DAPNET CORE PROJECT
 * Copyright (C) 2016
 *
 * Daniel Sialkowski
 *
 * daniel.sialkowski@rwth-aachen.de
 *
 * Institute of High Frequency Technology
 * RWTH AACHEN UNIVERSITY
 * Melatener Str. 25
 * 52074 Aachen
 */

package org.dapnet.core.transmission;

import java.time.Instant;

public class PagerMessage implements Comparable<PagerMessage> {
	private final String text;
	private final int address;
	private final Instant timestamp;
	private final MessagePriority priority;
	private final FunctionalBits functionalBits;

	public enum MessagePriority {
		EMERGENCY, TIME, CALL, NEWS, ACTIVATION, RUBRIC
	}

	public enum FunctionalBits {
		NUMERIC(0), TONE(1), ACTIVATION(2), ALPHANUM(3);

		private int value;

		private FunctionalBits(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	public PagerMessage(Instant timestamp, String text, int address, MessagePriority priority,
			FunctionalBits functionalBits) {
		this.text = text;
		this.address = address;
		this.timestamp = timestamp;
		this.priority = priority;
		this.functionalBits = functionalBits;
	}

	public PagerMessage(String text, int address, MessagePriority priority, FunctionalBits functionalBits) {
		this(Instant.now(), text, address, priority, functionalBits);
	}

	public String getText() {
		return text;
	}

	public int getAddress() {
		return address;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	public MessagePriority getPriority() {
		return priority;
	}

	public FunctionalBits getFunctionalBits() {
		return functionalBits;
	}

	@Override
	public int compareTo(PagerMessage o) {
		if (priority.ordinal() < o.priority.ordinal()) {
			return -1;
		} else if (priority.ordinal() > o.priority.ordinal()) {
			return 1;
		}

		// Same Priority, check Timestamp
		if (timestamp.isBefore(o.timestamp)) {
			return -1;
		} else if (timestamp.isAfter(o.timestamp)) {
			return 1;
		} else {
			return 0;
		}
	}

}
