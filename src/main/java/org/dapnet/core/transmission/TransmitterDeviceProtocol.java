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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

// general structure for paging protocols e.g. slave and master
public interface TransmitterDeviceProtocol {
	// enum to distinguish between different message types
	enum PagingMessageType {
		SYNCREQUEST(2), SYNCORDER(3), SLOTS(4), NUMERIC(5), ALPHANUM(6),;

		private int value;

		private PagingMessageType(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	void handleWelcome(TransmitterDevice transmitterDevice, PrintWriter toServer, BufferedReader fromServer)
			throws TransmitterDeviceException, IOException, InterruptedException;

	void handleMessage(Message message, PrintWriter toServer, BufferedReader fromServer)
			throws TransmitterDeviceException, IOException, InterruptedException;
}
