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

import org.dapnet.core.model.CoreRepository;
import org.dapnet.core.model.News;
import org.dapnet.core.rest.LoginData;
import org.dapnet.core.rest.RestSecurity;
import org.dapnet.core.rest.exceptionHandling.EmptyBodyException;

import com.mchange.rmi.NotAuthorizedException;

@Path("/news")
@Produces(MediaType.APPLICATION_JSON)
public class NewsResource extends AbstractResource {
	@GET
	public Response getNews(@QueryParam("rubricName") String rubricName) throws Exception {
		final CoreRepository repo = getRepository();
		Lock lock = repo.getLock().readLock();
		lock.lock();

		try {
			if (rubricName == null || rubricName.isEmpty()) {
				RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);
				return getObject(repo.getNews(), status);
			} else {
				RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY,
						repo.getRubrics().get(rubricName));
				return getObject(repo.getNews().get(rubricName), status);
			}
		} finally {
			lock.unlock();
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postNews(String newsJSON) throws Exception {
		News news = null;

		// Start request processing only if at least USER
		checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);

		// Create News
		news = getGsonProvider().getForRequest().fromJson(newsJSON, News.class);
		if (news != null) {
			news.setTimestamp(Instant.now());
			LoginData login = LoginData.fromHttpHeaders(httpHeaders);
			if (login != null) {
				news.setOwnerName(login.getUsername());
			} else {
				throw new NotAuthorizedException("Could not get login data.");
			}
		} else {
			throw new EmptyBodyException();
		}

		final CoreRepository repo = getRepository();
		Lock lock = repo.getLock().readLock();
		lock.lock();

		try {
			// Check if user is OWNER of rubric
			checkAuthorization(RestSecurity.SecurityLevel.OWNER_ONLY, repo.getRubrics().get(news.getRubricName()));
		} finally {
			lock.unlock();
		}

		return handleObject(news, "postNews", true, false);
	}
}
