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

package org.dapnet.core.rest.resources;

import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.dapnet.core.model.Activation;
import org.dapnet.core.rest.RestSecurity;
import org.dapnet.core.rest.exceptionHandling.EmptyBodyException;

@Path("/activation")
@Produces(MediaType.APPLICATION_JSON)
public class ActivationResource extends AbstractResource {
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postCall(String activationJSON) throws Exception {
		checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);

		// Create Activation
		Activation activation = gson.fromJson(activationJSON, Activation.class);
		if (activation != null) {
			activation.setTimestamp(new Date());
		} else {
			throw new EmptyBodyException();
		}

		return handleObject(activation, "postActivation", false, true);
	}
}
