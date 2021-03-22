package org.dapnet.core.rest;

import java.util.Objects;

import javax.ws.rs.ext.Provider;

import org.dapnet.core.model.CoreRepository;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * REST application configuration class for jersey.
 * 
 * @author Philipp Thiel
 */
@Provider
final class ApplicationConfig extends ResourceConfig {

	/**
	 * Constructs a new application config instance.
	 * 
	 * @param repository   Repository to use
	 * @param restListener REST listener to use
	 * @throws NullPointerException if any of the provided arguments is {@code null}
	 */
	public ApplicationConfig(CoreRepository repository, RestListener restListener) {
		CoreApplicationBinder binder = new CoreApplicationBinder(repository, restListener);

		packages("org/dapnet/core/rest");
		register(binder);
	}

	private static class CoreApplicationBinder extends AbstractBinder {

		private final CoreRepository repository;
		private final RestListener restListener;

		public CoreApplicationBinder(CoreRepository repository, RestListener restListener) {
			this.repository = Objects.requireNonNull(repository, "Repository must not be null.");
			this.restListener = Objects.requireNonNull(restListener, "REST listener must not be null.");
		}

		@Override
		protected void configure() {
			bind(repository).to(CoreRepository.class);
			bind(new RestSecurity(repository)).to(RestSecurity.class);
			bind(restListener).to(RestListener.class);
		}

	}

}
