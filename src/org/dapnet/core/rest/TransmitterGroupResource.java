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
import org.dapnet.core.model.TransmitterGroup;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/transmitterGroups")
public class TransmitterGroupResource extends AbstractResource {
    private static final Logger logger = LogManager.getLogger(TransmitterGroupResource.class.getName());
    protected Logger getLogger()
    {
        return logger;
    }

    @GET
    @Produces("application/json")
    public Response getTransmitterGroups(@Context HttpHeaders httpHeaders) {
        Response response = null;
        try {
            return response = getObject(httpHeaders, restListener.getState().getTransmitterGroups(),
                    RestSecurity.SecurityLevel.USER_ONLY, null
            );
        } finally {
            logResponse(response, RestMethod.GET, "All");
        }
    }

    @GET
    @Path("{transmitterGroup}")
    @Produces("application/json")
    public Response getTransmitterGroup(@Context HttpHeaders httpHeaders,
                                        @PathParam("transmitterGroup") String transmitterGroupName) {
        Response response = null;
        try {
            return response = getObject(
                    httpHeaders, restListener.getState().getTransmitterGroups().findByName(transmitterGroupName),
                    RestSecurity.SecurityLevel.USER_ONLY, null
            );
        } finally {
            logResponse(response, RestMethod.GET, transmitterGroupName);
        }
    }

    @PUT
    @Path("{transmitterGroup}")
    @Consumes("application/json")
    @Produces("application/json")
    public Response putTransmitterGroup(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders,
                                        @PathParam("transmitterGroup") String transmitterGroupName,
                                        String transmitterGroupJSON) {
        Response response = null;
        try {
            //Create TransmitterGroup
            TransmitterGroup transmitterGroup = gson.fromJson(transmitterGroupJSON, TransmitterGroup.class);
            transmitterGroup.setName(transmitterGroupName);

            if (restListener.getState().getTransmitterGroups().contains(transmitterGroupName)) {
                //Overwrite
                return response = handleObject(httpHeaders, uriInfo,
                        transmitterGroup,
                        RestSecurity.SecurityLevel.OWNER_ONLY,
                        restListener.getState().getTransmitterGroups().findByName(transmitterGroupName),
                        false,
                        "putTransmitterGroup", true);
            } else {
                //Create
                return response = handleObject(httpHeaders, uriInfo,
                        transmitterGroup,
                        RestSecurity.SecurityLevel.USER_ONLY,
                        null,
                        true,
                        "putTransmitterGroup", true);
            }
        }
        //Invalid JSON
        catch (JsonSyntaxException e) {
            return response = Response.status(Response.Status.BAD_REQUEST).entity("JSON ungültig").build();
        } finally {
            logResponse(response, RestMethod.PUT, transmitterGroupName);
        }
    }

    @DELETE
    @Produces("application/json")
    @Path("{transmitterGroup}")
    public Response deleteTransmitterGroup(@Context HttpHeaders httpHeaders,
                                           @PathParam("transmitterGroup") String transmitterGroup) {
        Response response = null;
        try {
            TransmitterGroup oldTransmitterGroup =
                    restListener.getState().getTransmitterGroups().findByName(transmitterGroup);
            if (oldTransmitterGroup != null) {
                return response = deleteObject(httpHeaders, oldTransmitterGroup,
                        RestSecurity.SecurityLevel.OWNER_ONLY, oldTransmitterGroup,
                        "deleteTransmitterGroup" , true);
            } else {
                return response = deleteObject(httpHeaders, null,
                        RestSecurity.SecurityLevel.USER_ONLY, null,
                        "deleteTransmitterGroup", true);
            }
        } finally {
            logResponse(response, RestMethod.DELETE, transmitterGroup);
        }
    }
}
