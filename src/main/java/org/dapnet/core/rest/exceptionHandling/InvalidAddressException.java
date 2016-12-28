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

package org.dapnet.core.rest.exceptionHandling;

import javax.ws.rs.WebApplicationException;

//used only to indicate that gson has a problem with an ipAddress
public class InvalidAddressException extends WebApplicationException {
	private static final long serialVersionUID = -810907789034331225L;
}
