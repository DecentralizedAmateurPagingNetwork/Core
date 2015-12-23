/*
 * DAPNET CORE PROJECT
 * Copyright (C) 2015
 *
 * Daniel Sialkowski
 *
 * daniel.sialkowski@rwth-aachen.de
 *
 * Institut für Hochfrequenztechnik
 * RWTH AACHEN UNIVERSITY
 * Melatener Str. 25
 * 52074 Aachen
 */

package org.dapnet.core.rest.resources;

import org.dapnet.core.model.Rubric;
import org.dapnet.core.rest.RestSecurity;
import org.dapnet.core.rest.ExceptionHandlingTemp.EmptyBodyException;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("/rubrics")
@Produces("application/json")
public class RubricResource extends AbstractResource {
    @GET
    public Response getRubrics() throws Exception {
        RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);
        return getObject(restListener.getState().getRubrics(), status);
    }

    @GET
    @Path("{rubric}")
    public Response getRubric(@PathParam("rubric") String rubricName) throws Exception {
        RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);
        return getObject(restListener.getState().getRubrics().findByName(rubricName), status);
    }

    @PUT
    @Path("{rubric}")
    @Consumes("application/json")
    public Response putRubric(@PathParam("rubric") String rubricName, String rubricJSON) throws Exception {
        if (restListener.getState().getRubrics().contains(rubricName)) { //Overwrite
            checkAuthorization(RestSecurity.SecurityLevel.OWNER_ONLY,
                    restListener.getState().getRubrics().findByName(rubricName));
        } else { //Create
            checkAuthorization(RestSecurity.SecurityLevel.ADMIN_ONLY);
        }

        //Create Rubric
        Rubric rubric = gson.fromJson(rubricJSON, Rubric.class);
        if(rubric!=null)
            rubric.setName(rubricName);
        else
            throw new EmptyBodyException();

        //TODO  Validate that there is no conflict with other Rubrics
        return handleObject(rubric, "putRubric", !restListener.getState().getRubrics().contains(rubricName), true);
    }

    @DELETE
    @Path("{rubric}")
    public Response deleteRubric(@PathParam("rubric") String rubric) throws Exception {
        Rubric oldRubric = restListener.getState().getRubrics().findByName(rubric);

        if (oldRubric != null) // only owner can delete object
            checkAuthorization(RestSecurity.SecurityLevel.OWNER_ONLY);
        else //only user will get message that object does not exist
            checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);

        return deleteObject(oldRubric, "deleteRubric", true);
    }
}
