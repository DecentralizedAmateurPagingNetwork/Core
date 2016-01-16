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

package org.dapnet.core.rest.exceptionHandling;

import javax.ws.rs.WebApplicationException;

//used only to indicate that gson has a problem with an ipAddress
public class InvalidAddressException extends WebApplicationException {
}
