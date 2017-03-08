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

import org.dapnet.core.HashUtil;
import org.dapnet.core.model.Node;
import org.dapnet.core.rest.RestSecurity;
import org.dapnet.core.rest.exceptionHandling.EmptyBodyException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/nodes")
@Produces(MediaType.APPLICATION_JSON)
public class NodeResource extends AbstractResource {
	@GET
	public Response getNodes() throws Exception {
		RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);
		return getObject(restListener.getState().getNodes().values(), status);
	}

	@GET
	@Path("{node}")
	public Response getNode(@PathParam("node") String nodeName) throws Exception {
		if (nodeName != null) {
			nodeName = nodeName.toLowerCase();
		}

		RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);
		return getObject(restListener.getState().getNodes().get(nodeName), status);
	}

	@PUT
	@Path("{node}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response putNode(@PathParam("node") String nodeName, String nodeJSON) throws Exception {
		if (nodeName != null) {
			nodeName = nodeName.toLowerCase();
		}

		checkAuthorization(RestSecurity.SecurityLevel.ADMIN_ONLY);

		// Create Node
		final Node node = gson.fromJson(nodeJSON, Node.class);
		if (node != null) {
			node.setName(nodeName);
			node.setKey(HashUtil.createHash(node.getKey()));
			node.setStatus(Node.Status.SUSPENDED);
		} else {
			throw new EmptyBodyException();
		}

		return handleObject(node, "putNode", !restListener.getState().getNodes().containsKey(nodeName), true);
	}

	@DELETE
	@Path("{node}")
	public Response deleteNode(@PathParam("node") String node) throws Exception {
		if (node != null) {
			node = node.toLowerCase();
		}

		checkAuthorization(RestSecurity.SecurityLevel.ADMIN_ONLY);
		return deleteObject(restListener.getState().getNodes().get(node), "deleteNode", true);
	}
}