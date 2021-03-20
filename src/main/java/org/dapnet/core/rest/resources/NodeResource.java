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

import java.util.concurrent.locks.Lock;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.dapnet.core.model.CoreRepository;
import org.dapnet.core.model.Node;
import org.dapnet.core.model.Node.Status;
import org.dapnet.core.rest.RestSecurity;
import org.dapnet.core.rest.exceptionHandling.EmptyBodyException;

@Path("/nodes")
@Produces(MediaType.APPLICATION_JSON)
public class NodeResource extends AbstractResource {
	@GET
	public Response getNodes() throws Exception {
		final CoreRepository repo = getRepository();
		Lock lock = repo.getLock().readLock();
		lock.lock();

		try {
			RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);
			return getObject(repo.getNodes().values(), status);
		} finally {
			lock.unlock();
		}
	}

	@GET
	@Path("{node}")
	public Response getNode(@PathParam("node") String nodeName) throws Exception {
		final CoreRepository repo = getRepository();
		Lock lock = repo.getLock().readLock();
		lock.lock();

		try {
			RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);
			return getObject(repo.getNodes().get(nodeName), status);
		} finally {
			lock.unlock();
		}
	}

	@PUT
	@Path("{node}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response putNode(@PathParam("node") String nodeName, String nodeJSON) throws Exception {
		Node node = null;
		Node oldNode = null;

		checkAuthorization(RestSecurity.SecurityLevel.ADMIN_ONLY);

		node = gson.fromJson(nodeJSON, Node.class);
		if (node != null) {
			node.setName(nodeName);
			node.setStatus(Node.Status.SUSPENDED);
		} else {
			throw new EmptyBodyException();
		}

		final CoreRepository repo = getRepository();
		Lock lock = repo.getLock().readLock();
		lock.lock();

		try {
			// Preserve old status
			oldNode = repo.getNodes().get(nodeName);
			if (oldNode != null && oldNode.getStatus() == Status.ONLINE) {
				node.setStatus(Status.ONLINE);
				node.setAddress(oldNode.getAddress());
			}
		} finally {
			lock.unlock();
		}

		return handleObject(node, "putNode", oldNode == null, true);
	}

	@DELETE
	@Path("{node}")
	public Response deleteNode(@PathParam("node") String node) throws Exception {
		Node oldNode = null;

		checkAuthorization(RestSecurity.SecurityLevel.ADMIN_ONLY);

		final CoreRepository repo = getRepository();
		Lock lock = repo.getLock().readLock();
		lock.lock();

		try {
			oldNode = repo.getNodes().get(node);
			if (oldNode == null) {
				throw new NotFoundException();
			}
		} finally {
			lock.unlock();
		}

		return deleteObject(oldNode, "deleteNode", true);
	}
}