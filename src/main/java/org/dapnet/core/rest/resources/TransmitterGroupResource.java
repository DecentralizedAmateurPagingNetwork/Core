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
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.dapnet.core.model.State;
import org.dapnet.core.model.TransmitterGroup;
import org.dapnet.core.rest.RestSecurity;
import org.dapnet.core.rest.exceptionHandling.EmptyBodyException;

@Path("/transmitterGroups")
@Produces(MediaType.APPLICATION_JSON)
public class TransmitterGroupResource extends AbstractResource {
	@GET
	public Response getTransmitterGroups() throws Exception {
		Lock lock = State.getReadLock();
		lock.lock();

		try {
			RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);
			return getObject(restListener.getState().getTransmitterGroups().values(), status);
		} finally {
			lock.unlock();
		}
	}

	@GET
	@Path("{transmitterGroup}")
	public Response getTransmitterGroup(@PathParam("transmitterGroup") String transmitterGroupName) throws Exception {
		if (transmitterGroupName != null) {
			transmitterGroupName = transmitterGroupName.toLowerCase();
		}

		Lock lock = State.getReadLock();
		lock.lock();

		try {
			TransmitterGroup obj = restListener.getState().getTransmitterGroups().get(transmitterGroupName);
			RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY, obj);
			return getObject(obj, status);
		} finally {
			lock.unlock();
		}
	}

	@PUT
	@Path("{transmitterGroup}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response putTransmitterGroup(@PathParam("transmitterGroup") String transmitterGroupName,
			String transmitterGroupJSON) throws Exception {
		if (transmitterGroupName != null) {
			transmitterGroupName = transmitterGroupName.toLowerCase();
		}

		TransmitterGroup oldGroup = null;
		TransmitterGroup transmitterGroup = null;

		Lock lock = State.getReadLock();
		lock.lock();

		try {
			oldGroup = restListener.getState().getTransmitterGroups().get(transmitterGroupName);
			if (oldGroup != null) {
				// Overwrite
				checkAuthorization(RestSecurity.SecurityLevel.OWNER_ONLY, oldGroup);
			} else {
				// Create
				checkAuthorization(RestSecurity.SecurityLevel.ADMIN_ONLY);
			}

			// Create TransmitterGroup
			transmitterGroup = gson.fromJson(transmitterGroupJSON, TransmitterGroup.class);
			if (transmitterGroup != null) {
				transmitterGroup.setName(transmitterGroupName);
			} else {
				throw new EmptyBodyException();
			}
		} finally {
			lock.unlock();
		}

		return handleObject(transmitterGroup, "putTransmitterGroup", oldGroup == null, true);
	}

	@DELETE
	@Path("{transmitterGroup}")
	public Response deleteTransmitterGroup(@PathParam("transmitterGroup") String transmitterGroup) throws Exception {
		if (transmitterGroup != null) {
			transmitterGroup = transmitterGroup.toLowerCase();
		}

		TransmitterGroup oldTransmitterGroup = null;

		Lock lock = State.getReadLock();
		lock.lock();

		try {
			oldTransmitterGroup = restListener.getState().getTransmitterGroups().get(transmitterGroup);
			if (oldTransmitterGroup != null) {
				checkAuthorization(RestSecurity.SecurityLevel.OWNER_ONLY, oldTransmitterGroup);
			} else {
				checkAuthorization(RestSecurity.SecurityLevel.ADMIN_ONLY);
				throw new NotFoundException();
			}
		} finally {
			lock.unlock();
		}

		return deleteObject(oldTransmitterGroup, "deleteTransmitterGroup", true);
	}
}
