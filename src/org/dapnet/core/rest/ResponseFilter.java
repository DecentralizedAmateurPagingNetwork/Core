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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.server.ContainerRequest;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class ResponseFilter implements ContainerResponseFilter {
    private static final Logger logger = LogManager.getLogger("REST_API");

    @Override
    public void filter(ContainerRequestContext request,
                       ContainerResponseContext response) throws IOException {
        response.getHeaders().add("Access-Control-Allow-Origin", "*");
        response.getHeaders().add("Access-Control-Allow-Headers",
                "Origin, X-Requested-With, Content-Type, Accept, Key, Authorization");
        response.getHeaders().add("Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE");

        if(response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL
                || response.getStatusInfo().getFamily() == Response.Status.Family.CLIENT_ERROR) {
            logger.info("Request: " + request.getMethod() + " "
                    + ((ContainerRequest) request).getPath(true) + " - "
                    + "Response: " + response.getStatusInfo());
        }
        else
        {
            logger.error("Request: " + request.getMethod() + " "
                    + ((ContainerRequest) request).getPath(true) + " - "
                    + "Response: " + response.getStatusInfo());
        }
    }
}