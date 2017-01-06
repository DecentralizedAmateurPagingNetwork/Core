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

import org.dapnet.core.model.State;
import org.jgroups.Address;

import java.util.Collection;

public interface RestListener {
	// Give (ReadOnly) Access to the State
	State getState();

	// Handler for WriteOnly Operations on State
	// Add here parameters for rollback
	@SuppressWarnings("rawtypes")
	boolean handleStateOperation(Collection<Address> destination, String methodName, Object[] args, Class[] types);

	// For pretest whether WriteOperation legal
	boolean isQuorum();
}
