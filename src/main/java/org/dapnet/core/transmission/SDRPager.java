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

public class SDRPager extends Raspager {
	private static final long serialVersionUID = 5883558421939398801L;

	public SDRPager(Transmitter transmitter, TransmitterDeviceListener listener) {
		super(transmitter, listener, DeviceType.SDRPAGER);
	}
}
