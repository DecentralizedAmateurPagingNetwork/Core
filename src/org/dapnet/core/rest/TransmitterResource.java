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
import org.dapnet.core.model.Transmitter;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/transmitters")
public class TransmitterResource extends AbstractResource {
    private static final Logger logger = LogManager.getLogger(TransmitterResource.class.getName());
    protected Logger getLogger()
    {
        return logger;
    }

    @GET
    @Produces("application/json")
    public Response getTransmitters(@Context HttpHeaders httpHeaders) {
        Response response = null;
        try {
            return response = getObject(httpHeaders, restListener.getState().getTransmitters(),
                    RestSecurity.SecurityLevel.USER_ONLY, null
            );
        } finally {
            logResponse(response, RestMethod.GET, "All");
        }
    }

    @GET
    @Path("{transmitter}")
    @Produces("application/json")
    public Response getTransmitter(@Context HttpHeaders httpHeaders,
                                   @PathParam("transmitter") String transmitterName) {
        Response response = null;
        try {
            return response = getObject(httpHeaders, restListener.getState().getTransmitters().findByName(transmitterName),
                    RestSecurity.SecurityLevel.USER_ONLY, null
            );
        } finally {
            logResponse(response, RestMethod.GET, transmitterName);
        }
    }

    @PUT
    @Path("{transmitter}")
    @Consumes("application/json")
    @Produces("application/json")
    public Response putTransmitter(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders,
                                   @PathParam("transmitter") String transmitterName, String transmitterJSON) {
        Response response = null;
        try {
            //Create Transmitter
            Transmitter transmitter = gson.fromJson(transmitterJSON, Transmitter.class);
            transmitter.setStatus(Transmitter.Status.OFFLINE);
            transmitter.setName(transmitterName);

            if (restListener.getState().getTransmitters().contains(transmitterName)) {
                //Overwrite
                return response = handleObject(httpHeaders, uriInfo,
                        transmitter,
                        RestSecurity.SecurityLevel.OWNER_ONLY,
                        restListener.getState().getTransmitters().findByName(transmitterName),
                        false, "putTransmitter", true);
            } else {
                //Create
                return response = handleObject(httpHeaders, uriInfo,
                        transmitter,
                        RestSecurity.SecurityLevel.USER_ONLY,
                        null,
                        true, "putTransmitter", true);
            }
        }
        //Invalid JSON
        catch (JsonSyntaxException e) {
            return response = Response.status(Response.Status.BAD_REQUEST).entity("JSON ungültig").build();

        } finally {
            logResponse(response, RestMethod.PUT, transmitterName);
        }
    }

    @DELETE
    @Produces("application/json")
    @Path("{transmitter}")
    public Response deleteTransmitter(@Context HttpHeaders httpHeaders, @PathParam("transmitter") String transmitter) {
        Response response = null;
        try {
            Transmitter oldTransmitter = restListener.getState().getTransmitters().findByName(transmitter);
            if (oldTransmitter != null) {
                return response = deleteObject(httpHeaders, oldTransmitter,
                        RestSecurity.SecurityLevel.OWNER_ONLY, oldTransmitter,
                        "deleteTransmitter", true);
            } else {
                return response = deleteObject(httpHeaders, null,
                        RestSecurity.SecurityLevel.USER_ONLY, null,
                        "deleteTransmitter", true);
            }
        } finally {
            logResponse(response, RestMethod.DELETE, transmitter);
        }
    }
}