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
import org.dapnet.core.model.Call;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Path("/calls")
public class CallResource extends AbstractResource {
    private static final Logger logger = LogManager.getLogger(CallResource.class.getName());
    protected Logger getLogger()
    {
        return logger;
    }

    @GET
    @Produces("application/json")
    public Response getCalls(@Context HttpHeaders httpHeaders, @QueryParam("ownerName") String ownerName) {
        Response response = null;
        try {
            if (ownerName == null || ownerName.isEmpty()) {
                return response = getObject(httpHeaders, restListener.getState().getCalls(), RestSecurity.SecurityLevel.ADMIN_ONLY, null
                );
            } else {
                List<Call> calls = new ArrayList<>();
                for (Call call : restListener.getState().getCalls()) {
                    if (call.getOwnerName().equals(ownerName))
                        calls.add(call);
                }
                return response = getObject(httpHeaders, calls, RestSecurity.SecurityLevel.OWNER_ONLY, restListener.getState().getUsers().findByName(ownerName)
                );
            }
        }
        //Internal Error
        catch (Exception e) {
            e.printStackTrace();
            return response = Response.serverError().build();
        } finally {
            logResponse(response, RestMethod.GET);
        }
    }

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response postCall(@Context HttpHeaders httpHeaders, @Context UriInfo uriInfo, String callJSON) {
        Response response = null;
        try {
            //Create Call
            Call call = gson.fromJson(callJSON, Call.class);
            call.setTimestamp(new Date());
            call.setOwnerName(restSecurity.getLoginData(httpHeaders).getUsername());

            return response = handleObject(httpHeaders, uriInfo,
                    call,
                    RestSecurity.SecurityLevel.USER_ONLY,
                    null,
                    false, "postCall", false);

        }
        //Invalid JSON
        catch (JsonSyntaxException e) {
            return response = Response.status(Response.Status.BAD_REQUEST).entity("JSON ungültig").build();

        }
        //Internal Error
        catch (Exception e) {
            e.printStackTrace();
            return response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            logResponse(response, RestMethod.POST);
        }
    }
}
