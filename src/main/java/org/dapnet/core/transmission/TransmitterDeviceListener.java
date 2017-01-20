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
	void onDeviceError(TransmitterDevice device, TransmitterDeviceException e);

	void onDeviceOffline(TransmitterDevice device, TransmitterDeviceException e);

	void onDeviceStarted(TransmitterDevice device);

	void onDeviceStopped(TransmitterDevice device);
}
