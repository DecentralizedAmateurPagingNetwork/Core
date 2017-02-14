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

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.dapnet.core.model.Transmitter;
import org.dapnet.core.rest.RestSecurity;
import org.dapnet.core.rest.exceptionHandling.EmptyBodyException;

@Path("/transmitters")
@Produces("application/json")
public class TransmitterResource extends AbstractResource {
	@GET
	public Response getTransmitters() throws Exception {
		RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);
		return getObject(restListener.getState().getTransmitters(), status);
	}

	@GET
	@Path("{transmitter}")
	public Response getTransmitter(@PathParam("transmitter") String transmitterName) throws Exception {
		if (transmitterName != null) {
			transmitterName = transmitterName.toLowerCase();
		}

		RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);
		return getObject(restListener.getState().getTransmitters().get(transmitterName), status);
	}

	@PUT
	@Path("{transmitter}")
	@Consumes("application/json")
	public Response putTransmitter(@PathParam("transmitter") String transmitterName, String transmitterJSON)
			throws Exception {
		if (transmitterName != null) {
			transmitterName = transmitterName.toLowerCase();
		}

		if (restListener.getState().getTransmitters().containsKey(transmitterName)) {
			// Overwrite
			checkAuthorization(RestSecurity.SecurityLevel.OWNER_ONLY,
					restListener.getState().getTransmitters().get(transmitterName));
		} else {
			// Create
			checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);
		}

		// Create Transmitter
		Transmitter transmitter = gson.fromJson(transmitterJSON, Transmitter.class);
		if (transmitter != null) {
			// Only Status OFFLINE or DISABLED is accepted:
			if (transmitter.getStatus() == null || transmitter.getStatus() != Transmitter.Status.DISABLED) {
				transmitter.setStatus(Transmitter.Status.OFFLINE);
			}

			transmitter.setName(transmitterName);
		} else {
			throw new EmptyBodyException();
		}

		return handleObject(transmitter, "putTransmitter",
				!restListener.getState().getTransmitters().containsKey(transmitterName), true);
	}

	@DELETE
	@Path("{transmitter}")
	public Response deleteTransmitter(@PathParam("transmitter") String transmitter) throws Exception {
		if (transmitter != null) {
			transmitter = transmitter.toLowerCase();
		}

		Transmitter oldTransmitter = restListener.getState().getTransmitters().get(transmitter);
		if (oldTransmitter != null) {
			checkAuthorization(RestSecurity.SecurityLevel.OWNER_ONLY);
		} else {
			checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);
		}

		return deleteObject(oldTransmitter, "deleteTransmitter", true);
	}
}