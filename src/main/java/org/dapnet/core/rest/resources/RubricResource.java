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
import javax.ws.rs.core.Response;

import org.dapnet.core.model.CoreRepository;
import org.dapnet.core.model.Rubric;
import org.dapnet.core.rest.RestSecurity;
import org.dapnet.core.rest.exceptionHandling.EmptyBodyException;

@Path("/rubrics")
@Produces("application/json")
public class RubricResource extends AbstractResource {
	@GET
	public Response getRubrics() throws Exception {
		RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);

		final CoreRepository repo = getRepository();
		Lock lock = repo.getLock().readLock();
		lock.lock();

		try {
			return getObject(repo.getRubrics().values(), status);
		} finally {
			lock.unlock();
		}
	}

	@GET
	@Path("{rubric}")
	public Response getRubric(@PathParam("rubric") String rubricName) throws Exception {
		final CoreRepository repo = getRepository();
		Lock lock = repo.getLock().readLock();
		lock.lock();

		try {
			Rubric obj = repo.getRubrics().get(rubricName);
			RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY, obj);
			return getObject(obj, status);
		} finally {
			lock.unlock();
		}
	}

	@PUT
	@Path("{rubric}")
	@Consumes("application/json")
	public Response putRubric(@PathParam("rubric") String rubricName, String rubricJSON) throws Exception {
		Rubric oldRubric = null;

		final CoreRepository repo = getRepository();
		Lock lock = repo.getLock().readLock();
		lock.lock();

		try {
			oldRubric = repo.getRubrics().get(rubricName);
			if (oldRubric != null) {
				// Overwrite
				checkAuthorization(RestSecurity.SecurityLevel.OWNER_ONLY, oldRubric);
			} else {
				// Create
				checkAuthorization(RestSecurity.SecurityLevel.ADMIN_ONLY);
			}
		} finally {
			lock.unlock();
		}

		// Create Rubric
		Rubric rubric = gson.fromJson(rubricJSON, Rubric.class);
		if (rubric != null) {
			rubric.setName(rubricName);
		} else {
			throw new EmptyBodyException();
		}

		return handleObject(rubric, "putRubric", oldRubric == null, true);
	}

	@DELETE
	@Path("{rubric}")
	public Response deleteRubric(@PathParam("rubric") String rubric) throws Exception {
		Rubric oldRubric = null;

		final CoreRepository repo = getRepository();
		Lock lock = repo.getLock().readLock();
		lock.lock();

		try {
			oldRubric = repo.getRubrics().get(rubric);
			if (oldRubric != null) {
				// only owner can delete object
				checkAuthorization(RestSecurity.SecurityLevel.OWNER_ONLY, oldRubric);
			} else {
				// only user will get message that object does not exist
				checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);
			}
		} finally {
			lock.unlock();
		}

		return deleteObject(oldRubric, "deleteRubric", true);
	}
}
