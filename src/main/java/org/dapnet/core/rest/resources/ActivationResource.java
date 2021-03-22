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
import java.util.Collection;
import java.util.concurrent.locks.Lock;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.dapnet.core.model.Activation;
import org.dapnet.core.model.CallSign;
import org.dapnet.core.model.Pager;
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
			activation.setTimestamp(Instant.now());
		} else {
			throw new EmptyBodyException();
		}

		// TODO We should probably move this to a validator that is added to Activation
		if (isRicRegistered(activation.getNumber())) {
			throw new BadRequestException("RIC already registered.");
		}

		return handleObject(activation, "postActivation", false, true);
	}

	private boolean isRicRegistered(int ric) {
		Lock lock = getRepository().getLock().readLock();
		lock.lock();

		try {
			final Collection<CallSign> callsigns = getRepository().getCallSigns().values();
			for (CallSign cs : callsigns) {
				for (Pager pgr : cs.getPagers()) {
					if (pgr.getNumber() == ric) {
						return true;
					}
				}
			}
		} finally {
			lock.unlock();
		}

		return false;
	}

}
