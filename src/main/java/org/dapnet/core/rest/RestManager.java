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

import java.net.BindException;
import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.CoreStartupException;
import org.dapnet.core.Settings;
import org.dapnet.core.rest.resources.AbstractResource;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

public class RestManager {
	private static final Logger logger = LogManager.getLogger();

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
			RestSettings settings = Settings.getRestSettings();

			ResourceConfig rc = new ResourceConfig().packages("org/dapnet/core/rest");
			URI endpoint = new URI("http", null, settings.getHostname(), settings.getPort(), settings.getPath(), null,
					null);
			server = GrizzlyHttpServerFactory.createHttpServer(endpoint, rc);
			logger.info("RestApi successfully started.");
		} catch (Exception e) {
			if (e.getCause() instanceof BindException) {
				logger.fatal("Starting RestApi failed: {}", e.getCause().getMessage());
			} else {
				logger.fatal("Starting RestApi failed.", e);
			}

			throw new CoreStartupException(e);
		}
	}

	public void stopServer() {
		if (server != null) {
			server.shutdownNow();
			logger.info("RestApi successfully stopped.");
		}
	}
}
