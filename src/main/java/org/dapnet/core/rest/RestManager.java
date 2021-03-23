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
import org.dapnet.core.model.CoreRepository;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * The REST manager implementation. This class is responsible for providing the
 * REST server.
 * 
 * @author Philipp Thiel
 */
public final class RestManager {
	private static final Logger logger = LogManager.getLogger();
	private final Settings settings;
	private final CoreRepository repository;
	private final RestListener restListener;
	private HttpServer server;

	/**
	 * Constructs a new REST manager instance.
	 * 
	 * @param repository   Repository to use
	 * @param restListener REST listener to use
	 */
	public RestManager(Settings settings, CoreRepository repository, RestListener restListener) {
		this.settings = Objects.requireNonNull(settings, "Settings must not be null.");
		this.repository = Objects.requireNonNull(repository, "State manager must not be null.");
		this.restListener = Objects.requireNonNull(restListener, "REST listener must not be null.");
	}

	/**
	 * Starts the REST server asynchronously.
	 */
	public void start() {
		try {
			URI endpoint = new URI("http", null, settings.getRestSettings().getHostname(),
					settings.getRestSettings().getPort(), settings.getRestSettings().getPath(), null, null);
			ResourceConfig rc = new ApplicationConfig(settings, repository, restListener);
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

	/**
	 * Stops the rest manager.
	 */
	public void stop() {
		if (server != null) {
			server.shutdownNow();
			logger.info("RestApi successfully stopped.");
		}
	}
}
