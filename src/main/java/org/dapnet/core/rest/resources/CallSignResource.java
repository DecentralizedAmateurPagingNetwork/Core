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

import java.util.concurrent.locks.Lock;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.dapnet.core.model.CallSign;
import org.dapnet.core.model.State;
import org.dapnet.core.rest.RestSecurity;
import org.dapnet.core.rest.exceptionHandling.EmptyBodyException;

@Path("/callsigns")
@Produces(MediaType.APPLICATION_JSON)
public class CallSignResource extends AbstractResource {
	@GET
	public Response getCallSigns() throws Exception {
		Lock lock = State.getReadLock();
		lock.lock();

		try {
			RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);
			return getObject(restListener.getState().getCallSigns().values(), status);
		} finally {
			lock.unlock();
		}
	}

	@GET
	@Path("{callSign}")
	public Response getCallSign(@PathParam("callSign") String callSignName) throws Exception {
		if (callSignName != null) {
			callSignName = callSignName.toLowerCase();
		}

		Lock lock = State.getReadLock();
		lock.lock();

		try {
			CallSign obj = restListener.getState().getCallSigns().get(callSignName);
			RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY, obj);
			return getObject(obj, status);
		} finally {
			lock.unlock();
		}
	}

	@PUT
	@Path("{callSign}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response putCallSign(@PathParam("callSign") String callSignName, String callSignJSON) throws Exception {
		if (callSignName != null) {
			callSignName = callSignName.toLowerCase();
		}

		CallSign oldCallSign = null;
		CallSign callSign = null;

		Lock lock = State.getReadLock();
		lock.lock();

		try {
			oldCallSign = restListener.getState().getCallSigns().get(callSignName);
			if (oldCallSign != null) {
				// Overwrite
				checkAuthorization(RestSecurity.SecurityLevel.OWNER_ONLY, oldCallSign);
			} else {
				// Create
				checkAuthorization(RestSecurity.SecurityLevel.ADMIN_ONLY);
			}

			callSign = gson.fromJson(callSignJSON, CallSign.class);
			if (callSign != null) {
				callSign.setName(callSignName);
			} else {
				throw new EmptyBodyException();
			}
		} finally {
			lock.unlock();
		}

		return handleObject(callSign, "putCallSign", oldCallSign == null, true);
	}

	@DELETE
	@Path("{callSign}")
	public Response deleteCallSign(@PathParam("callSign") String callSign) throws Exception {
		if (callSign != null) {
			callSign = callSign.toLowerCase();
		}

		CallSign oldCallSign = null;

		Lock lock = State.getReadLock();
		lock.lock();

		try {

			oldCallSign = restListener.getState().getCallSigns().get(callSign);
			if (oldCallSign != null) {
				// only owner can delete object
				checkAuthorization(RestSecurity.SecurityLevel.OWNER_ONLY, oldCallSign);
			} else {
				// only user will get message that object does not exist
				checkAuthorization(RestSecurity.SecurityLevel.ADMIN_ONLY);
			}
		} finally {
			lock.unlock();
		}

		return deleteObject(oldCallSign, "deleteCallSign", true);
	}
}
