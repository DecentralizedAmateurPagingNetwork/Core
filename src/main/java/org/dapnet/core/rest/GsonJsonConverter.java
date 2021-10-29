package org.dapnet.core.rest;

import org.dapnet.core.Settings;
import org.dapnet.core.rest.RestSecurity.SecurityStatus;
import org.jgroups.stack.IpAddress;

import com.google.gson.ExclusionStrategy;
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

	/**
	 * Constructs a new object instance.
	 * 
	 * @param settings Settings to use
	 * @throws NullPointerException if {@code settings == null}
	 */
	public GsonJsonConverter(Settings settings) {
		if (settings == null) {
			throw new NullPointerException("Settings must not be null.");
		}

		adminGson = createGson(settings, ExclusionStrategies.ADMIN);
		userGson = createGson(settings, ExclusionStrategies.USER);
	}

	/**
	 * Creates a configured Gson instance.
	 * 
	 * @param settings              Settings to use
	 * @param serializationStrategy Optional exclusion strategy to add for
	 *                              serialization
	 * @return Gson instance
	 */
	private static Gson createGson(Settings settings, ExclusionStrategy serializationStrategy) {
		GsonBuilder builder = new GsonBuilder();
		builder.serializeNulls();
		builder.setPrettyPrinting();
		builder.registerTypeAdapterFactory(new GsonTypeAdapterFactory());
		builder.registerTypeAdapter(String.class, new StringTrimJsonDeserializer());

		if (settings.getRestSettings().jsonFilterIpAddresses()) {
			ExclusionStrategy ipFilter = new ExclusionStrategies.SpecificClassFilter(IpAddress.class);
			builder.addSerializationExclusionStrategy(ipFilter);
		}

		if (serializationStrategy != null) {
			builder.addSerializationExclusionStrategy(serializationStrategy);
		}

		return builder.create();
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

	/**
	 * Gets the proper Gson instance for the given security status.
	 * 
	 * @param status Security status
	 * @return Gson instance
	 */
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
