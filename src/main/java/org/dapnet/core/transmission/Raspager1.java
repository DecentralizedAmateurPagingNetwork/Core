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

public class Raspager1 extends Raspager {
	private static final long serialVersionUID = -2558125969580027551L;

	public Raspager1(Transmitter transmitter, TransmitterDeviceListener listener) {
		super(transmitter, listener, DeviceType.RASPPAGER1);
	}
}
