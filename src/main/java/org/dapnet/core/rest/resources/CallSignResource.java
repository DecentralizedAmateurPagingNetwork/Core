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
import javax.ws.rs.core.Response;

@Path("/callsigns")
@Produces("application/json")
public class CallSignResource extends AbstractResource {
	@GET
	public Response getCallSigns() throws Exception {
		RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);
		return getObject(restListener.getState().getCallSigns(), status);
	}

	@GET
	@Path("{callSign}")
	public Response getCallSign(@PathParam("callSign") String callSignName) throws Exception {
		RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);
		return getObject(restListener.getState().getCallSigns().findByName(callSignName), status);
	}

	@PUT
	@Path("{callSign}")
	@Consumes("application/json")
	public Response putCallSign(@PathParam("callSign") String callSignName, String callSignJSON) throws Exception {
		if (restListener.getState().getCallSigns().contains(callSignName)) { // Overwrite
			checkAuthorization(RestSecurity.SecurityLevel.OWNER_ONLY,
					restListener.getState().getCallSigns().findByName(callSignName));
		} else { // Create
			checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);
		}

		CallSign callSign = gson.fromJson(callSignJSON, CallSign.class);
		if (callSign != null)
			callSign.setName(callSignName);
		else // gson couldnt create an object
			throw new EmptyBodyException();

		return handleObject(callSign, "putCallSign", !restListener.getState().getCallSigns().contains(callSignName),
				true);
	}

	@DELETE
	@Path("{callSign}")
	public Response deleteCallSign(@PathParam("callSign") String callSign) throws Exception {
		CallSign oldCallSign = restListener.getState().getCallSigns().findByName(callSign);

		if (oldCallSign != null) // only owner can delete object
			checkAuthorization(RestSecurity.SecurityLevel.OWNER_ONLY);
		else // only user will get message that object does not exist
			checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);

		return deleteObject(oldCallSign, "deleteCallSign", true);
	}
}
