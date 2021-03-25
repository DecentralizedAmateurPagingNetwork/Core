package org.dapnet.core.rest;

import org.dapnet.core.Settings;
import org.dapnet.core.rest.RestSecurity.SecurityStatus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * JSON converter implementation using Gson.
 * 
 * @author Philipp Thiel
 */
final class GsonJsonConverter implements JsonConverter {

	private final Gson adminGson;
	private final Gson userGson;

	public GsonJsonConverter(Settings settings) {
		if (settings == null) {
			throw new NullPointerException("Settings must not be null.");
		}

		adminGson = createBuilder().addSerializationExclusionStrategy(ExclusionStrategies.ADMIN).create();
		userGson = createBuilder().setExclusionStrategies(ExclusionStrategies.USER).create();
	}

	private static GsonBuilder createBuilder() {
		GsonBuilder builder = new GsonBuilder();
		builder.serializeNulls();
		builder.setPrettyPrinting();
		builder.registerTypeAdapterFactory(new GsonTypeAdapterFactory());
		builder.registerTypeAdapter(String.class, new StringTrimJsonDeserializer());

		return builder;
	}

	@Override
	public <T> T fromJson(String json, Class<T> type) {
		return adminGson.fromJson(json, type);
	}

	@Override
	public String toJson(Object source) {
		return adminGson.toJson(source);
	}

	@Override
	public String toJson(Object source, SecurityStatus status) {
		final Gson gson = getForStatus(status);
		return gson.toJson(source);
	}

	private Gson getForStatus(SecurityStatus status) {
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

}
