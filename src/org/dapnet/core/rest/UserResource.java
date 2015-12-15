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
import org.dapnet.core.HashUtil;
import org.dapnet.core.model.User;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/users")
public class UserResource extends AbstractResource {
    private static final Logger logger = LogManager.getLogger(UserResource.class.getName());
    protected Logger getLogger()
    {
        return logger;
    }

    @GET
    @Produces("application/json")
    public Response getUsers(@Context HttpHeaders httpHeaders) {
        Response response = null;
        try {
            return response = getObject(httpHeaders, restListener.getState().getUsers(),
                    RestSecurity.SecurityLevel.USER_ONLY, null
            );
        } finally {
            logResponse(response, AbstractResource.RestMethod.GET, "All");
        }
    }

    @GET
    @Path("{user}")
    @Produces("application/json")
    public Response getUser(@Context HttpHeaders httpHeaders, @PathParam("user") String userName) {
        Response response = null;
        try {
            return response = getObject(httpHeaders, restListener.getState().getUsers().findByName(userName),
                    RestSecurity.SecurityLevel.USER_ONLY, null
            );
        } finally {
            logResponse(response, AbstractResource.RestMethod.GET, userName);
        }
    }

    @PUT
    @Path("{user}")
    @Consumes("application/json")
    @Produces("application/json")
    public Response putUser(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders,
                            @PathParam("user") String userName, String userJSON) {
        Response response = null;
        try {
            //Create User
            User user = gson.fromJson(userJSON, User.class);
            user.setHash(HashUtil.createHash(user.getHash()));
            user.setName(userName);

            if (restListener.getState().getUsers().contains(userName)) {
                //Overwrite
                return response = handleObject(httpHeaders, uriInfo,
                        user,
                        //Only admins can overwrite admins!
                        user.isAdmin() ? RestSecurity.SecurityLevel.ADMIN_ONLY : RestSecurity.SecurityLevel.OWNER_ONLY,
                        restListener.getState().getUsers().findByName(userName),
                        false,
                        "putUser", true);
            } else {
                //Create
                return response = handleObject(httpHeaders, uriInfo,
                        user,
                        //Only admins can create admins!
                        user.isAdmin() ? RestSecurity.SecurityLevel.ADMIN_ONLY : RestSecurity.SecurityLevel.USER_ONLY,
                        null,
                        true,
                        "putUser", true);
            }
        }
        //Invalid JSON
        catch (JsonSyntaxException e) {
            return response = Response.status(Response.Status.BAD_REQUEST).entity("JSON ungültig").build();
        } catch (Exception e) {
            return response = Response.serverError().build();
        } finally {
            logResponse(response, RestMethod.PUT, userName);
        }
    }

    @DELETE
    @Produces("application/json")
    @Path("{user}")
    public Response deleteUser(@Context HttpHeaders httpHeaders, @PathParam("user") String user) {
        Response response = null;
        try {
            User oldUser = restListener.getState().getUsers().findByName(user);
            if (oldUser != null) {
                return response = deleteObject(httpHeaders, oldUser,
                        RestSecurity.SecurityLevel.OWNER_ONLY, oldUser,
                        "deleteUser", true);
            } else {
                return response = deleteObject(httpHeaders, null,
                        RestSecurity.SecurityLevel.USER_ONLY, null,
                        "deleteUser", true);
            }
        } finally {
            logResponse(response, RestMethod.DELETE, user);
        }
    }
}
