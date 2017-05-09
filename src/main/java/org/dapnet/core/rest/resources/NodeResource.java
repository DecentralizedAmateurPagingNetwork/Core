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

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.dapnet.core.model.Node;
import org.dapnet.core.rest.RestSecurity;
import org.dapnet.core.rest.exceptionHandling.EmptyBodyException;

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

		final Node node = gson.fromJson(nodeJSON, Node.class);
		if (node != null) {
			node.setName(nodeName);
			node.setStatus(Node.Status.SUSPENDED);
		} else {
			throw new EmptyBodyException();
		}

		// Preserve old status
		Node oldNode = restListener.getState().getNodes().get(nodeName);
		if (oldNode != null) {
			node.setStatus(oldNode.getStatus());
		}

		return handleObject(node, "putNode", oldNode == null, true);
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