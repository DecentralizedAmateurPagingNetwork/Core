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
import org.dapnet.core.model.Node;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/nodes")
public class NodeResource extends AbstractResource {
    private static final Logger logger = LogManager.getLogger(NodeResource.class.getName());
    protected Logger getLogger()
    {
        return logger;
    }

    @GET
    @Produces("application/json")
    public Response getNodes(@Context HttpHeaders httpHeaders) {
        Response response = null;
        try {
            return response = getObject(httpHeaders, restListener.getState().getNodes(),
                    RestSecurity.SecurityLevel.USER_ONLY, null
            );
        } finally {
            logResponse(response, AbstractResource.RestMethod.GET, "All");
        }
    }

    @GET
    @Path("{node}")
    @Produces("application/json")
    public Response getNode(@Context HttpHeaders httpHeaders, @PathParam("node") String nodeName) {
        Response response = null;
        try {
            return response = getObject(httpHeaders, restListener.getState().getNodes().findByName(nodeName),
                    RestSecurity.SecurityLevel.USER_ONLY, null
            );
        } finally {
            logResponse(response, RestMethod.GET, nodeName);
        }
    }

    @PUT
    @Path("{node}")
    @Consumes("application/json")
    @Produces("application/json")
    public Response putNode(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders,
                            @PathParam("node") String nodeName, String nodeJSON) {
        Response response = null;
        try {
            //Create Node
            Node node = gson.fromJson(nodeJSON, Node.class);
            node.setName(nodeName);
            node.setKey(HashUtil.createHash(node.getKey()));

            return response = handleObject(httpHeaders, uriInfo,
                    node,
                    RestSecurity.SecurityLevel.ADMIN_ONLY,
                    null,
                    !restListener.getState().getNodes().contains(nodeName),
                    "putNode", true);
        }
        //Invalid JSON
        catch (JsonSyntaxException e) {
            return response = Response.status(Response.Status.BAD_REQUEST).entity("JSON ungültig").build();
        } catch (Exception e) {
            return response = Response.serverError().build();
        } finally {
            logResponse(response, RestMethod.PUT, nodeName);
        }
    }

    @DELETE
    @Produces("application/json")
    @Path("{node}")
    public Response deleteNode(@Context HttpHeaders httpHeaders, @PathParam("node") String node) {
        Response response = null;
        try {
            Node oldNode = restListener.getState().getNodes().findByName(node);
            if (oldNode != null) {
                return response = deleteObject(httpHeaders, oldNode,
                        RestSecurity.SecurityLevel.ADMIN_ONLY, null,
                        "deleteNode", true);
            } else {
                return response = deleteObject(httpHeaders, null,
                        RestSecurity.SecurityLevel.ADMIN_ONLY, null,
                        "deleteNode", true);
            }
        } finally {
            logResponse(response, RestMethod.DELETE, node);
        }
    }
}