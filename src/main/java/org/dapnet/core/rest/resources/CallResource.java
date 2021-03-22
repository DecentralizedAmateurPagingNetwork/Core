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

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.dapnet.core.model.Call;
import org.dapnet.core.model.CoreRepository;
import org.dapnet.core.rest.LoginData;
import org.dapnet.core.rest.RestSecurity;
import org.dapnet.core.rest.exceptionHandling.EmptyBodyException;

import com.mchange.rmi.NotAuthorizedException;

@Path("/calls")
@Produces(MediaType.APPLICATION_JSON)
public class CallResource extends AbstractResource {

	@GET
	public Response getCalls(@QueryParam("ownerName") String ownerName) throws Exception {
		final CoreRepository repo = getRepository();
		Lock lock = repo.getLock().readLock();
		lock.lock();

		try {
			if (ownerName == null || ownerName.isEmpty()) {
				RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.ADMIN_ONLY);
				return getObject(repo.getCalls(), status);
			} else {
				RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.OWNER_ONLY,
						repo.getUsers().get(ownerName));

				List<Call> calls = new LinkedList<>();
				for (Call call : repo.getCalls()) {
					if (call.getOwnerName().equalsIgnoreCase(ownerName)) {
						calls.add(call);
					}
				}

				return getObject(calls, status);
			}
		} finally {
			lock.unlock();
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postCall(String callJSON) throws Exception {
		checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);

		// Create Call
		Call call = gson.fromJson(callJSON, Call.class);
		if (call != null) {
			call.setTimestamp(Instant.now());
			LoginData login = LoginData.fromHttpHeaders(httpHeaders);
			if (login != null) {
				call.setOwnerName(login.getUsername());
			} else {
				throw new NotAuthorizedException("Could not get login data.");
			}
		} else {
			throw new EmptyBodyException();
		}

		return handleObject(call, "postCall", true, false);
	}
}
