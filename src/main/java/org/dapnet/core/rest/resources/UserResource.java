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
import org.dapnet.core.model.User;
import org.dapnet.core.rest.RestSecurity;
import org.dapnet.core.rest.exceptionHandling.EmptyBodyException;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("/users")
@Produces("application/json")
public class UserResource extends AbstractResource {
	@GET
	public Response getUsers() throws Exception {
		RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);
		return getObject(restListener.getState().getUsers(), status);
	}

	@GET
	@Path("{user}")
	public Response getUser(@PathParam("user") String userName) throws Exception {
		RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);
		return getObject(restListener.getState().getUsers().findByName(userName), status);
	}

	@PUT
	@Path("{user}")
	@Consumes("application/json")
	public Response putUser(@PathParam("user") String userName, String userJSON) throws Exception {
		// Start request processing only if at least USER
		checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);

		// Create User
		User user = gson.fromJson(userJSON, User.class);
		if (user != null) {
			user.setHash(HashUtil.createHash(user.getHash()));
			user.setName(userName);
		} else
			throw new EmptyBodyException();

		if (user.isAdmin())
			checkAuthorization(RestSecurity.SecurityLevel.ADMIN_ONLY);
		else {
			if (restListener.getState().getUsers().contains(userName))
				checkAuthorization(RestSecurity.SecurityLevel.OWNER_ONLY,
						restListener.getState().getUsers().findByName(userName));
			else
				checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);
		}

		return handleObject(user, "putUser", !restListener.getState().getUsers().contains(userName), true);
	}

	@DELETE
	@Path("{user}")
	public Response deleteUser(@PathParam("user") String user) throws Exception {
		User oldUser = restListener.getState().getUsers().findByName(user);

		if (oldUser != null) // only owner can delete object
			checkAuthorization(RestSecurity.SecurityLevel.OWNER_ONLY);
		else // only user will get message that object does not exist
			checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);

		return deleteObject(oldUser, "deleteUser", true);
	}
}
