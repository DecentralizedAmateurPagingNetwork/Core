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

import java.time.Instant;
import java.util.concurrent.locks.Lock;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.dapnet.core.model.News;
import org.dapnet.core.model.State;
import org.dapnet.core.rest.LoginData;
import org.dapnet.core.rest.RestSecurity;
import org.dapnet.core.rest.exceptionHandling.EmptyBodyException;

@Path("/news")
@Produces(MediaType.APPLICATION_JSON)
public class NewsResource extends AbstractResource {
	@GET
	public Response getNews(@QueryParam("rubricName") String rubricName) throws Exception {
		Lock lock = State.getReadLock();
		lock.lock();

		try {
			if (rubricName == null || rubricName.isEmpty()) {
				RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);
				return getObject(restListener.getState().getNews(), status);
			} else {
				rubricName = rubricName.toLowerCase();
				RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY,
						restListener.getState().getRubrics().get(rubricName));
				return getObject(restListener.getState().getNews().get(rubricName), status);
			}
		} finally {
			lock.unlock();
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postNews(String newsJSON) throws Exception {
		News news = null;

		Lock lock = State.getReadLock();
		lock.lock();

		try {
			// Start request processing only if at least USER
			checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);

			// Create News
			news = gson.fromJson(newsJSON, News.class);
			if (news != null) {
				news.setTimestamp(Instant.now());
				news.setOwnerName(new LoginData(httpHeaders).getUsername());
			} else {
				throw new EmptyBodyException();
			}

			// Check if user is OWNER of rubric
			checkAuthorization(RestSecurity.SecurityLevel.OWNER_ONLY,
					restListener.getState().getRubrics().get(news.getRubricName()));
		} finally {
			lock.unlock();
		}

		return handleObject(news, "postNews", true, false);
	}
}
