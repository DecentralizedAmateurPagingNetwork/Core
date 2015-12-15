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
import org.dapnet.core.model.News;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Path("/news")
public class NewsResource extends AbstractResource {
    private static final Logger logger = LogManager.getLogger(NewsResource.class.getName());
    protected Logger getLogger()
    {
        return logger;
    }

    @GET
    @Produces("application/json")
    public Response getNews(@Context HttpHeaders httpHeaders, @QueryParam("rubricName") String rubricName) {
        Response response = null;
        try {
            if (rubricName == null || rubricName.isEmpty()) {
                return response = getObject(httpHeaders, restListener.getState().getNews(), RestSecurity.SecurityLevel.ADMIN_ONLY, null
                );
            } else {
                List<News> newsList = new ArrayList<>();
                for (News news: restListener.getState().getNews()) {
                    if (news.getRubricName().equals(rubricName))
                        newsList.add(news);
                }
                return response = getObject(httpHeaders, newsList, RestSecurity.SecurityLevel.OWNER_ONLY, restListener.getState().getRubrics().findByName(rubricName)
                );
            }
        }
        //Internal Error
        catch (Exception e) {
            e.printStackTrace();
            return response = Response.serverError().build();
        } finally {
            logResponse(response, RestMethod.GET);
        }
    }

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response postNews(@Context HttpHeaders httpHeaders, @Context UriInfo uriInfo, String newsJSON) {
        Response response = null;
        try {
            //Create News
            News news = gson.fromJson(newsJSON, News.class);
            news.setTimestamp(new Date());
            news.setOwnerName(restSecurity.getLoginData(httpHeaders).getUsername());

            return response = handleObject(httpHeaders, uriInfo,
                    news,
                    RestSecurity.SecurityLevel.OWNER_ONLY,
                    restListener.getState().getRubrics().findByName(news.getRubricName()),
                    false, "postNews", false);

        }
        //Invalid JSON
        catch (JsonSyntaxException e) {
            return response = Response.status(Response.Status.BAD_REQUEST).entity("JSON ungültig").build();

        }
        //Internal Error
        catch (Exception e) {
            e.printStackTrace();
            return response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            logResponse(response, RestMethod.POST);
        }
    }
}
