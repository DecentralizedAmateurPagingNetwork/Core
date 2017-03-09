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

import java.io.IOException;
import java.net.UnknownHostException;
import java.time.Instant;

import org.dapnet.core.rest.exceptionHandling.InvalidAddressException;
import org.jgroups.stack.IpAddress;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class GsonTypeAdapter implements TypeAdapterFactory {

	@SuppressWarnings("unchecked")
	@Override
	public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> tokenType) {
		Class<?> rawType = tokenType.getRawType();
		if (Instant.class.isAssignableFrom(rawType)) {
			return (TypeAdapter<T>) new InstantTypeAdapter();
		} else {
			TypeAdapter<T> adapter = gson.getDelegateAdapter(this, tokenType);
			return new GenericTypeAdapter<T>(adapter, gson);
		}
	}

	private static class GenericTypeAdapter<T> extends TypeAdapter<T> {
		private final TypeAdapter<T> adapter;
		private final Gson gson;

		public GenericTypeAdapter(TypeAdapter<T> adapter, Gson gson) {
			this.adapter = adapter;
			this.gson = gson;
		}

		// Validation Workaround because IpAddress is already validated
		// while conversation from JSON to object
		@Override
		public T read(JsonReader reader) throws IOException {
			T returnValue;
			// Check IpAddress
			try {
				returnValue = adapter.read(reader);
			} catch (UnknownHostException e) {
				throw new InvalidAddressException();
			}

			// Check Port
			if (returnValue instanceof IpAddress) {
				int port = ((IpAddress) returnValue).getPort();
				if (port > 65535 || port < 1) {
					// Valid ports are between 1 and 65535
					throw new InvalidAddressException();
				}
			}

			return returnValue;
		}

		@Override
		public void write(JsonWriter writer, T value) throws IOException {
			JsonElement tree = adapter.toJsonTree(value);

			// Add hostname to json output of IpAddress
			// if (value instanceof IpAddress) {
			// String host = ((IpAddress)
			// value).getIpAddress().getHostName();
			// JsonObject jo = (JsonObject) tree;
			// jo.addProperty("hostname", host);
			// }

			gson.getAdapter(JsonElement.class).write(writer, tree);
		}
	}

	private static class InstantTypeAdapter extends TypeAdapter<Instant> {

		@Override
		public void write(JsonWriter out, Instant value) throws IOException {
			if (value == null) {
				out.nullValue();
			} else {
				out.value(value.toString());
			}
		}

		@Override
		public Instant read(JsonReader in) throws IOException {
			if (in.peek() == JsonToken.NULL) {
				in.nextNull();
				return null;
			}

			String raw = in.nextString();
			return Instant.parse(raw);
		}
	}
}
