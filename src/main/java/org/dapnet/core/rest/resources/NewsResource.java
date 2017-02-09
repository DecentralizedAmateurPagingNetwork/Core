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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.dapnet.core.model.News;
import org.dapnet.core.rest.LoginData;
import org.dapnet.core.rest.RestSecurity;
import org.dapnet.core.rest.exceptionHandling.EmptyBodyException;

@Path("/news")
@Produces("application/json")
public class NewsResource extends AbstractResource {
	@GET
	public Response getNews(@QueryParam("rubricName") String rubricName) throws Exception {
		if (rubricName == null || rubricName.isEmpty()) {
			RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.ADMIN_ONLY);
			return getObject(restListener.getState().getNews(), status);
		} else {
			RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.OWNER_ONLY,
					restListener.getState().getRubrics().get(rubricName));

			List<News> newsList = new ArrayList<>();
			for (News news : restListener.getState().getNews()) {
				if (news.getRubricName().equals(rubricName))
					newsList.add(news);
			}
			return getObject(newsList, status);
		}

	}

	@POST
	@Consumes("application/json")
	public Response postNews(String newsJSON) throws Exception {
		// Start request processing only if at least USER
		checkAuthorization(RestSecurity.SecurityLevel.USER_ONLY);

		// Create News
		News news = gson.fromJson(newsJSON, News.class);
		if (news != null) {
			news.setTimestamp(new Date());
			news.setOwnerName(new LoginData(httpHeaders).getUsername());
		} else
			throw new EmptyBodyException();

		// Check whether OWNER of rubric
		checkAuthorization(RestSecurity.SecurityLevel.OWNER_ONLY,
				restListener.getState().getRubrics().get(news.getRubricName()));

		return handleObject(news, "postNews", true, false);
	}
}
