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

import org.dapnet.core.model.CallSign;
import org.dapnet.core.rest.RestSecurity;
import org.dapnet.core.rest.exceptionHandling.EmptyBodyException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/callsigns")
@Produces(MediaType.APPLICATION_JSON)
public class CallSignResource extends AbstractResource {
	@GET
	public Response getCallSigns() throws Exception {
		RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);
		return getObject(restListener.getState().getCallSigns().values(), status);
	}

	@GET
	@Path("{callSign}")
	public Response getCallSign(@PathParam("callSign") String callSignName) throws Exception {
		if (callSignName != null) {
			callSignName = callSignName.toLowerCase();
		}

		RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);
		return getObject(restListener.getState().getCallSigns().get(callSignName), status);
	}

	@PUT
	@Path("{callSign}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response putCallSign(@PathParam("callSign") String callSignName, String callSignJSON) throws Exception {
		if (callSignName != null) {
			callSignName = callSignName.toLowerCase();
		}

		final CallSign oldCallSign = restListener.getState().getCallSigns().get(callSignName);
		if (oldCallSign != null) {
			// Overwrite
			checkAuthorization(RestSecurity.SecurityLevel.OWNER_ONLY, oldCallSign);
		} else {
			// Create
			checkAuthorization(RestSecurity.SecurityLevel.ADMIN_ONLY);
		}

		final CallSign callSign = gson.fromJson(callSignJSON, CallSign.class);
		if (callSign != null) {
			callSign.setName(callSignName);
		} else {
			throw new EmptyBodyException();
		}

		return handleObject(callSign, "putCallSign", oldCallSign == null, true);
	}

	@DELETE
	@Path("{callSign}")
	public Response deleteCallSign(@PathParam("callSign") String callSign) throws Exception {
		if (callSign != null) {
			callSign = callSign.toLowerCase();
		}

		final CallSign oldCallSign = restListener.getState().getCallSigns().get(callSign);
		if (oldCallSign != null) {
			// only owner can delete object
			checkAuthorization(RestSecurity.SecurityLevel.OWNER_ONLY);
		} else {
			// only user will get message that object does not exist
			checkAuthorization(RestSecurity.SecurityLevel.ADMIN_ONLY);
		}

		return deleteObject(oldCallSign, "deleteCallSign", true);
	}
}
