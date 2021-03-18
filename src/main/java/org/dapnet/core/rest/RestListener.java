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

package org.dapnet.core.rest;

import java.util.Collection;

import org.jgroups.Address;

public interface RestListener {
	// Handler for WriteOnly Operations on State
	// Add here parameters for rollback
	@SuppressWarnings("rawtypes")
	boolean handleStateOperation(Collection<Address> destination, String methodName, Object[] args, Class[] types);

	// For pretest whether WriteOperation legal
	boolean isQuorum();
}
