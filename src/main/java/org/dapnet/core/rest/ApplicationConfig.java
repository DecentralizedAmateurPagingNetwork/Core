package org.dapnet.core.rest;

import java.util.Objects;

import org.dapnet.core.model.StateManager;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * REST application configuration class for jersey.
 * 
 * @author Philipp Thiel
 */
final class ApplicationConfig extends ResourceConfig {

	/**
	 * Constructs a new application config instance.
	 * 
	 * @param stateManager State manager to use
	 * @param restListener REST listener to use
	 * @throws NullPointerException if any of the provided arguments is {@code null}
	 */
	public ApplicationConfig(StateManager stateManager, RestListener restListener) {
		CoreApplicationBinder binder = new CoreApplicationBinder(stateManager, restListener);

		packages("org/dapnet/core/rest");
		register(binder);
	}

	private static class CoreApplicationBinder extends AbstractBinder {

		private final StateManager stateManager;
		private final RestListener restListener;

		public CoreApplicationBinder(StateManager stateManager, RestListener restListener) {
			this.stateManager = Objects.requireNonNull(stateManager, "State manager must not be null.");
			this.restListener = Objects.requireNonNull(restListener, "REST listener must not be null.");
		}

		@Override
		protected void configure() {
			bind(stateManager);
			bind(new RestSecurity(stateManager));
			bind(restListener);
		}

	}

}
