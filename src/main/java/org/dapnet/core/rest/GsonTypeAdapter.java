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

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.dapnet.core.rest.exceptionHandling.InvalidAddressException;
import org.jgroups.stack.IpAddress;

import java.io.IOException;
import java.net.UnknownHostException;

public class GsonTypeAdapter implements TypeAdapterFactory {
	@Override
	public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> tokenType) {
		final TypeAdapter<T> adapter = gson.getDelegateAdapter(this, tokenType);

		return new TypeAdapter<T>() {
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
					if (port > 65535 || port < 1) // Valid ports are between 1
													// and 65535
						throw new InvalidAddressException();
				}
				return returnValue;
			}

			@Override
			public void write(JsonWriter writer, T value) throws IOException {
				JsonElement tree = adapter.toJsonTree(value);

				// Add hostname to json output of IpAddress
				if (value instanceof IpAddress) {
					String host = ((IpAddress) value).getIpAddress().getHostName();
					JsonObject jo = (JsonObject) tree;
					jo.addProperty("hostname", host);
				}

				gson.getAdapter(JsonElement.class).write(writer, tree);
			}
		};
	}
}
