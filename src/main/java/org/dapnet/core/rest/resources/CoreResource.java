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

import org.dapnet.core.DAPNETCore;
import org.dapnet.core.rest.RestSecurity;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/core")
@Produces("application/json")
public class CoreResource extends AbstractResource {
    @Path("/core_version")
    @GET
    public Response getCoreVersion() throws Exception {
        RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.EVERYBODY);
        return getObject(DAPNETCore.getCoreVersion(), status);
    }

    @Path("/api_version")
    @GET
    public Response getApiVersion() throws Exception {
        RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.EVERYBODY);
        return getObject(DAPNETCore.getApiVersion(), status);
    }
}
