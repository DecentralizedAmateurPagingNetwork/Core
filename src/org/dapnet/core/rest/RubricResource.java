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

package org.dapnet.core.rest;

import com.google.gson.JsonSyntaxException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.model.Rubric;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/rubrics")
public class RubricResource extends AbstractResource {
    private static final Logger logger = LogManager.getLogger(RubricResource.class.getName());
    protected Logger getLogger()
    {
        return logger;
    }

    @GET
    @Produces("application/json")
    public Response getRubrics(@Context HttpHeaders httpHeaders) {
        Response response = null;
        try {
            return response = getObject(httpHeaders, restListener.getState().getRubrics(),
                    RestSecurity.SecurityLevel.USER_ONLY, null
            );
        } finally {
            logResponse(response, RestMethod.GET, "All");
        }
    }

    @GET
    @Path("{rubric}")
    @Produces("application/json")
    public Response getRubric(@Context HttpHeaders httpHeaders, @PathParam("rubric") String rubricName) {
        Response response = null;
        try {
            return response = getObject(httpHeaders, restListener.getState().getRubrics().findByName(rubricName),
                    RestSecurity.SecurityLevel.USER_ONLY, null
            );
        } finally {
            logResponse(response, RestMethod.GET, rubricName);
        }
    }

    @PUT
    @Path("{rubric}")
    @Consumes("application/json")
    @Produces("application/json")
    public Response putRubric(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders,
                              @PathParam("rubric") String rubricName, String rubricJSON) {
        Response response = null;
        try {
            //Create Rubric
            Rubric rubric = gson.fromJson(rubricJSON, Rubric.class);
            rubric.setName(rubricName);

            //TODO  Validate that there is no conflict with other Rubric

            if (restListener.getState().getRubrics().contains(rubricName)) {
                //Overwrite
                return response = handleObject(httpHeaders, uriInfo,
                        rubric,
                        RestSecurity.SecurityLevel.OWNER_ONLY,
                        restListener.getState().getRubrics().findByName(rubricName),
                        false,
                        "putRubric", true);
            } else {
                //Create
                return response = handleObject(httpHeaders, uriInfo,
                        rubric,
                        RestSecurity.SecurityLevel.ADMIN_ONLY,
                        null,
                        true,
                        "putRubric", true);
            }
        }
        //Invalid JSON
        catch (JsonSyntaxException e) {
            return response = Response.status(Response.Status.BAD_REQUEST).entity("JSON ungültig").build();

        } finally {
            logResponse(response, RestMethod.PUT, rubricName);
        }
    }

    @DELETE
    @Produces("application/json")
    @Path("{rubric}")
    public Response deleteRubric(@Context HttpHeaders httpHeaders, @PathParam("rubric") String rubric) {
        Response response = null;
        try {
            Rubric oldRubric = restListener.getState().getRubrics().findByName(rubric);
            if (oldRubric != null) {
                return response = deleteObject(httpHeaders, oldRubric,
                        RestSecurity.SecurityLevel.OWNER_ONLY, oldRubric,
                        "deleteRubric", true);
            } else {
                return response = deleteObject(httpHeaders, null,
                        RestSecurity.SecurityLevel.USER_ONLY, null,
                        "deleteRubric" , true);
            }
        } finally {
            logResponse(response, RestMethod.DELETE, rubric);
        }
    }
}
