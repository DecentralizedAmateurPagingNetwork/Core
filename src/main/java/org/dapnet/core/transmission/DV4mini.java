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

public class DV4mini extends Raspager {
	private static final long serialVersionUID = 3949091578203640141L;

	public DV4mini(Transmitter transmitter, TransmitterDeviceListener listener) {
		super(transmitter, listener, DeviceType.DV4mini);
	}
}
