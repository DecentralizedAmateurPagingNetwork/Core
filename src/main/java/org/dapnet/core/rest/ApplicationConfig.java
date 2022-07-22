package org.dapnet.core.rest;

import java.util.Objects;

import javax.ws.rs.ext.Provider;

import org.dapnet.core.Settings;
import org.dapnet.core.cluster.RemoteMethods;
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
	 * @param settings   Settings to use
	 * @param repository Repository to use
	 * @param rpcMethods RPC methods to use
	 * @throws NullPointerException if any of the provided arguments is {@code null}
	 */
	public ApplicationConfig(Settings settings, CoreRepository repository, RemoteMethods rpcMethods) {
		CoreApplicationBinder binder = new CoreApplicationBinder(settings, repository, rpcMethods);

		packages("org/dapnet/core/rest");
		register(binder);
	}

	private static class CoreApplicationBinder extends AbstractBinder {

		private final Settings settings;
		private final CoreRepository repository;
		private final RemoteMethods rpcMethods;

		public CoreApplicationBinder(Settings settings, CoreRepository repository, RemoteMethods rpcMethods) {
			this.settings = Objects.requireNonNull(settings, "Settings must not be null.");
			this.repository = Objects.requireNonNull(repository, "Repository must not be null.");
			this.rpcMethods = Objects.requireNonNull(rpcMethods, "REST listener must not be null.");
		}

		@Override
		protected void configure() {
			bind(settings).to(Settings.class);
			bind(repository).to(CoreRepository.class);
			bind(new RestSecurity(repository)).to(RestSecurity.class);
			bind(rpcMethods).to(RemoteMethods.class);
			bind(new GsonJsonConverter(settings)).to(JsonConverter.class);
		}

	}

}
