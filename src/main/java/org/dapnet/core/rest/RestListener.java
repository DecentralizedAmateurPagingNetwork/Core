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

/**
 * REST listener interface.
 * 
 * @author Philipp Thiel
 */
public interface RestListener {

	/**
	 * Issues an RPC to the cluster.
	 * 
	 * @param destination Destination address
	 * @param methodName  Method name
	 * @param args        Method arguments
	 * @param types       Argument types
	 * @return State operation result
	 */
	@SuppressWarnings("rawtypes")
	boolean handleStateOperation(Collection<Address> destination, String methodName, Object[] args, Class[] types);

	/**
	 * Tests if the cluster currently has a quorum, i.e. is in a healthy state.
	 * 
	 * @return Quorum status
	 */
	boolean isQuorum();
}
