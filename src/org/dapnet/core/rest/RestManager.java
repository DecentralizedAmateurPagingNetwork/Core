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

package org.dapnet.core.rest;


import com.sun.net.httpserver.HttpServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.DAPNETCore;
import org.dapnet.core.Settings;
import org.dapnet.core.rest.resources.AbstractResource;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;

public class RestManager {
    private static final Logger logger = LogManager.getLogger(RestManager.class.getName());

    private HttpServer server;
    private RestListener restListener;
    private RestSecurity restSecurity;

    public RestManager(RestListener restListener) {
        this.restListener = restListener;
        this.restSecurity = new RestSecurity(this.restListener);

        AbstractResource.setRestListener(this.restListener);
        AbstractResource.setRestSecurity(restSecurity);
    }

    public void startServer() {
        try {
            ResourceConfig rc = new ResourceConfig().packages("org/dapnet/core/rest");
            URI endpoint = new URI("http://localhost:" + Settings.getRestSettings().getPort() + "/");
            server = JdkHttpServerFactory.createHttpServer(endpoint, rc);
            logger.info("RestApi successfully started");
        } catch (Exception e) {
            logger.fatal("Starting RestApi failed");
            logger.catching(e);
            DAPNETCore.stopDAPNETCore();
        }
    }

    public void stopServer() {
        if (server != null) {
            server.stop(0);
            logger.info("RestApi successfully stopped");
        } else {
            logger.error("Stopping RestApi failed");
        }
    }
}
