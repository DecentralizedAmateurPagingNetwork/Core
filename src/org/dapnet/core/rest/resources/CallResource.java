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

import org.dapnet.core.model.Call;
import org.dapnet.core.rest.LoginData;
import org.dapnet.core.rest.RestSecurity;
import org.dapnet.core.rest.exceptionHandling.EmptyBodyException;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Path("/calls")
@Produces("application/json")
public class CallResource extends AbstractResource {
    @GET
    public Response getCalls(@QueryParam("ownerName") String ownerName) throws Exception {
            if (ownerName == null || ownerName.isEmpty()) {
                RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.ADMIN_ONLY);
                return getObject(restListener.getState().getCalls(),status);
            } else {
                RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.OWNER_ONLY,
                                            restListener.getState().getUsers().findByName(ownerName));

                List<Call> calls = new ArrayList<>();
                for (Call call : restListener.getState().getCalls()) {
                    if (call.getOwnerName().equals(ownerName))
                        calls.add(call);
                }
                return getObject(calls, status);
            }
    }

    @POST
    @Consumes("application/json")
    public Response postCall(String callJSON) throws Exception {
        checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);

        //Create Call
        Call call = gson.fromJson(callJSON, Call.class);
        if(call != null) {
            call.setTimestamp(new Date());
            call.setOwnerName(new LoginData(httpHeaders).getUsername());
        }
        else
            throw new EmptyBodyException();

        return handleObject(call, "postCall", true, false);
    }
}
