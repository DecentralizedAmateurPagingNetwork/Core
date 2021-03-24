package org.dapnet.core.rest;

import org.dapnet.core.Settings;
import org.dapnet.core.rest.RestSecurity.SecurityStatus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

final class GsonProviderImpl implements GsonProvider {

	private final Gson adminGson;
	private final Gson userGson;

	public GsonProviderImpl(Settings settings) {
		if (settings == null) {
			throw new NullPointerException("Settings must not be null.");
		}

		adminGson = createBuilder().addSerializationExclusionStrategy(ExclusionStrategies.ADMIN).create();
		userGson = createBuilder().setExclusionStrategies(ExclusionStrategies.USER).create();
	}

	@Override
	public Gson getForRequest() {
		return adminGson;
	}

	@Override
	public Gson getForResponse() {
		return adminGson;
	}

	@Override
	public Gson getForResponse(SecurityStatus status) {
		switch (status) {
		case ADMIN:
			return adminGson;
		case OWNER:
			return adminGson;
		case USER:
			return userGson;
		case ANYBODY:
			return userGson;
		default:
			// TODO Good idea to have admin view as the default?
			return adminGson;
		}
	}

	private static GsonBuilder createBuilder() {
		GsonBuilder builder = new GsonBuilder();
		builder.serializeNulls();
		builder.setPrettyPrinting();
		builder.registerTypeAdapterFactory(new GsonTypeAdapterFactory());
		builder.registerTypeAdapter(String.class, new StringTrimJsonDeserializer());

		return builder;
	}

}
