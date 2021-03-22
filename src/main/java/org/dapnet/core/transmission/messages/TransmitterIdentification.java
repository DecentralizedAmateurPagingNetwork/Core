package org.dapnet.core.transmission.messages;

import java.util.Objects;

public class TransmitterIdentification {

	private final String identification;
	private final int address;

	public TransmitterIdentification(String identification, int address) {
		this.identification = Objects.requireNonNull(identification, "Identification must not be null.");
		this.address = address;
	}

	public String getIdentification() {
		return identification;
	}

	public int getAddress() {
		return address;
	}

}
