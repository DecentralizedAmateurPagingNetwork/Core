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

import org.dapnet.core.model.Transmitter;

/**
 * Transmitter manager event listener interface.
 */
public interface TransmitterManagerListener {
	/**
	 * Called when a transmitter status has changed.
	 * 
	 * @param transmitter Affected transmitter
	 */
	void handleTransmitterStatusChanged(Transmitter transmitter);

	/**
	 * Called when all transmitters are to be disconnected.
	 */
	void handleDisconnectFromAllTransmitters();
}
