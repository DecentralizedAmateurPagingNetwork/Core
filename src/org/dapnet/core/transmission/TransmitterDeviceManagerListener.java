/*
 * DAPNET CORE PROJECT
 * Copyright (C) 2016
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

import org.dapnet.core.model.Transmitter;

public interface TransmitterDeviceManagerListener {
    void handleTransmitterStatusChanged(String transmitterName, Transmitter.Status newStatus);
    void handleDisconnectedFromAllTransmitters();
}
