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

import java.util.Date;

public class Message implements Comparable<Message> {
	private String text;
	private int address;
	private Date timestamp;
	private MessagePriority priority;
	private FunctionalBits functionalBits;

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

	public Message(String text, int address, MessagePriority priority, FunctionalBits functionalBits) {
		this.text = text;
		this.address = address;
		this.timestamp = new Date();
		this.priority = priority;
		this.functionalBits = functionalBits;
	}

	public String getText() {
		return text;
	}

	public int getAddress() {
		return address;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public MessagePriority getPriority() {
		return priority;
	}

	public FunctionalBits getFunctionalBits() {
		return functionalBits;
	}

	@Override
	public int compareTo(Message message) {
		if (priority.ordinal() < message.getPriority().ordinal())
			return -1;
		if (priority.ordinal() > message.getPriority().ordinal())
			return 1;

		// Same Priority, check Timestamp
		if (timestamp.before(message.getTimestamp()))
			return -1;
		if (timestamp.after(message.getTimestamp()))
			return 1;

		// Also same Timestamp:
		return 0;
	}
}
