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

import org.dapnet.core.model.Rubric;
import org.dapnet.core.rest.RestSecurity;
import org.dapnet.core.rest.exceptionHandling.EmptyBodyException;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("/rubrics")
@Produces("application/json")
public class RubricResource extends AbstractResource {
	@GET
	public Response getRubrics() throws Exception {
		RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);
		return getObject(restListener.getState().getRubrics().values(), status);
	}

	@GET
	@Path("{rubric}")
	public Response getRubric(@PathParam("rubric") String rubricName) throws Exception {
		if (rubricName != null) {
			rubricName = rubricName.toLowerCase();
		}

		RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);
		return getObject(restListener.getState().getRubrics().get(rubricName), status);
	}

	@PUT
	@Path("{rubric}")
	@Consumes("application/json")
	public Response putRubric(@PathParam("rubric") String rubricName, String rubricJSON) throws Exception {
		if (rubricName != null) {
			rubricName = rubricName.toLowerCase();
		}

		if (restListener.getState().getRubrics().containsKey(rubricName)) {
			// Overwrite
			checkAuthorization(RestSecurity.SecurityLevel.OWNER_ONLY,
					restListener.getState().getRubrics().get(rubricName));
		} else {
			// Create
			checkAuthorization(RestSecurity.SecurityLevel.ADMIN_ONLY);
		}

		// Create Rubric
		Rubric rubric = gson.fromJson(rubricJSON, Rubric.class);
		if (rubric != null) {
			rubric.setName(rubricName);
		} else {
			throw new EmptyBodyException();
		}

		return handleObject(rubric, "putRubric", !restListener.getState().getRubrics().containsKey(rubricName), true);
	}

	@DELETE
	@Path("{rubric}")
	public Response deleteRubric(@PathParam("rubric") String rubric) throws Exception {
		if (rubric != null) {
			rubric = rubric.toLowerCase();
		}

		Rubric oldRubric = restListener.getState().getRubrics().get(rubric);
		if (oldRubric != null) {
			// only owner can delete object
			checkAuthorization(RestSecurity.SecurityLevel.OWNER_ONLY);
		} else {
			// only user will get message that object does not exist
			checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);
		}

		return deleteObject(oldRubric, "deleteRubric", true);
	}
}
