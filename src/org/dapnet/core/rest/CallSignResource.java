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
import org.dapnet.core.model.CallSign;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/callsigns")
public class CallSignResource extends AbstractResource {
    private static final Logger logger = LogManager.getLogger(CallSignResource.class.getName());
    protected Logger getLogger()
    {
        return logger;
    }

    @GET
    @Produces("application/json")
    public Response getCallSigns(@Context HttpHeaders httpHeaders) {
        Response response = null;
        try {
            return response = getObject(httpHeaders, restListener.getState().getCallSigns(),
                    RestSecurity.SecurityLevel.USER_ONLY, null
            );
        } finally {
            logResponse(response, RestMethod.GET, "All");
        }
    }

    @GET
    @Path("{callSign}")
    @Produces("application/json")
    public Response getCallSign(@Context HttpHeaders httpHeaders, @PathParam("callSign") String callSignName) {
        Response response = null;
        try {
            return response = getObject(httpHeaders, restListener.getState().getCallSigns().findByName(callSignName),
                    RestSecurity.SecurityLevel.USER_ONLY, null
            );
        } finally {
            logResponse(response, RestMethod.GET, callSignName);
        }
    }

    @PUT
    @Path("{callSign}")
    @Consumes("application/json")
    @Produces("application/json")
    public Response putCallSign(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders,
                                @PathParam("callSign") String callSignName, String callSignJSON) {
        Response response = null;
        try {
            //Create CallSign
            CallSign callSign = gson.fromJson(callSignJSON, CallSign.class);
            callSign.setName(callSignName);

            if (restListener.getState().getCallSigns().contains(callSignName)) {
                //Overwrite
                return response = handleObject(httpHeaders, uriInfo,
                        callSign,
                        RestSecurity.SecurityLevel.OWNER_ONLY,
                        restListener.getState().getCallSigns().findByName(callSignName),
                        false, "putCallSign", true);
            } else {
                //Create
                return response = handleObject(httpHeaders, uriInfo,
                        callSign,
                        RestSecurity.SecurityLevel.USER_ONLY,
                        null,
                        true, "putCallSign", true);
            }
        }
        //Invalid JSON
        catch (JsonSyntaxException e) {
            return response = Response.status(Response.Status.BAD_REQUEST).entity("JSON ungültig").build();

        } finally {
            logResponse(response, RestMethod.PUT, callSignName);
        }
    }

    @DELETE
    @Produces("application/json")
    @Path("{callSign}")
    public Response deleteCallSign(@Context HttpHeaders httpHeaders, @PathParam("callSign") String callSign) {
        Response response = null;
        try {
            CallSign oldCallSign = restListener.getState().getCallSigns().findByName(callSign);
            if (oldCallSign != null) {
                return response = deleteObject(httpHeaders, oldCallSign,
                        RestSecurity.SecurityLevel.OWNER_ONLY, oldCallSign,
                        "deleteCallSign", true);
            } else {
                return response = deleteObject(httpHeaders, null,
                        RestSecurity.SecurityLevel.USER_ONLY, null,
                        "deleteCallSign", true);
            }
        } finally {
            logResponse(response, RestMethod.DELETE, callSign);
        }
    }
}
