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

public interface TransmitterDeviceListener {
	void handleTransmitterDeviceError(TransmitterDevice transmitterDevice, TransmitterDeviceException e);

	void handleTransmitterDeviceOffline(TransmitterDevice transmitterDevice, TransmitterDeviceException e);

	void handleTransmitterDeviceStarted(TransmitterDevice transmitterDevice);

	void handleTransmitterDeviceStopped(TransmitterDevice transmitterDevice);
}
