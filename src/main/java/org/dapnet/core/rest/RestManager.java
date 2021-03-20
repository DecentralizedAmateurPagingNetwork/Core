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
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.CoreStartupException;
import org.dapnet.core.Settings;
import org.dapnet.core.model.StateManager;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

public class RestManager {
	private static final Logger logger = LogManager.getLogger();
	private final StateManager stateManager;
	private final RestListener restListener;
	private HttpServer server;

	public RestManager(StateManager stateManager, RestListener restListener) {
		this.stateManager = Objects.requireNonNull(stateManager, "State manager must not be null.");
		this.restListener = Objects.requireNonNull(restListener, "REST listener must not be null.");
	}

	public void start() {
		try {
			final RestSettings settings = Settings.getRestSettings();
			URI endpoint = new URI("http", null, settings.getHostname(), settings.getPort(), settings.getPath(), null,
					null);
			ResourceConfig rc = new ApplicationConfig(stateManager, restListener);
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

	public void stop() {
		if (server != null) {
			server.shutdownNow();
			logger.info("RestApi successfully stopped.");
		}
	}
}
