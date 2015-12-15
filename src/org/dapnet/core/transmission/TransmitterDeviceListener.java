/*
 * DAPNET CORE PROJECT
 * Copyright (C) 2015
 *
 * Daniel Sialkowski
 *
 * daniel.sialkowski@rwth-aachen.de
 *
 * Institut für Hochfrequenztechnik
 * RWTH AACHEN UNIVERSITY
 * Melatener Str. 25
 * 52074 Aachen
 */

package org.dapnet.core.transmission;

public interface TransmitterDeviceListener {
    void handleTransmitterDeviceError(TransmitterDevice transmitterDevice, TransmitterDeviceException e);
    void handleTransmitterDeviceStarted(TransmitterDevice transmitterDevice);
    void handleTransmitterDeviceStopped(TransmitterDevice transmitterDevice);
}
