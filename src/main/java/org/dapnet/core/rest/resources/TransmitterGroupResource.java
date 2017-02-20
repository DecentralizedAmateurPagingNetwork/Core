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

import org.dapnet.core.model.TransmitterGroup;
import org.dapnet.core.rest.RestSecurity;
import org.dapnet.core.rest.exceptionHandling.EmptyBodyException;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("/transmitterGroups")
@Produces("application/json")
public class TransmitterGroupResource extends AbstractResource {
	@GET
	public Response getTransmitterGroups() throws Exception {
		RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);
		return getObject(restListener.getState().getTransmitterGroups().values(), status);
	}

	@GET
	@Path("{transmitterGroup}")
	public Response getTransmitterGroup(@PathParam("transmitterGroup") String transmitterGroupName) throws Exception {
		if (transmitterGroupName != null) {
			transmitterGroupName = transmitterGroupName.toLowerCase();
		}

		RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);
		return getObject(restListener.getState().getTransmitterGroups().get(transmitterGroupName), status);
	}

	@PUT
	@Path("{transmitterGroup}")
	@Consumes("application/json")
	public Response putTransmitterGroup(@PathParam("transmitterGroup") String transmitterGroupName,
			String transmitterGroupJSON) throws Exception {
		if (transmitterGroupName != null) {
			transmitterGroupName = transmitterGroupName.toLowerCase();
		}

		if (restListener.getState().getTransmitterGroups().containsKey(transmitterGroupName)) {
			// Overwrite
			checkAuthorization(RestSecurity.SecurityLevel.OWNER_ONLY,
					restListener.getState().getTransmitterGroups().get(transmitterGroupName));
		} else {
			// Create
			checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);
		}

		// Create TransmitterGroup
		TransmitterGroup transmitterGroup = gson.fromJson(transmitterGroupJSON, TransmitterGroup.class);
		if (transmitterGroup != null) {
			transmitterGroup.setName(transmitterGroupName);
		} else {
			throw new EmptyBodyException();
		}

		return handleObject(transmitterGroup, "putTransmitterGroup",
				!restListener.getState().getTransmitterGroups().containsKey(transmitterGroupName), true);
	}

	@DELETE
	@Path("{transmitterGroup}")
	public Response deleteTransmitterGroup(@PathParam("transmitterGroup") String transmitterGroup) throws Exception {
		if (transmitterGroup != null) {
			transmitterGroup = transmitterGroup.toLowerCase();
		}

		TransmitterGroup oldTransmitterGroup = restListener.getState().getTransmitterGroups().get(transmitterGroup);
		if (oldTransmitterGroup != null) {
			checkAuthorization(RestSecurity.SecurityLevel.OWNER_ONLY);
			return deleteObject(oldTransmitterGroup, "deleteTransmitterGroup", true);
		} else {
			checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);
			throw new NotFoundException();
		}
	}
}
