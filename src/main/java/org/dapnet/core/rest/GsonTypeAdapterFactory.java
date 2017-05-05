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
import java.util.LinkedList;

import org.dapnet.core.model.News;
import org.dapnet.core.model.NewsList;
import org.dapnet.core.rest.exceptionHandling.InvalidAddressException;
import org.jgroups.stack.IpAddress;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class GsonTypeAdapterFactory implements TypeAdapterFactory {

	@SuppressWarnings("unchecked")
	@Override
	public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> tokenType) {
		Class<?> rawType = tokenType.getRawType();
		if (Instant.class.isAssignableFrom(rawType)) {
			return (TypeAdapter<T>) new InstantTypeAdapter();
		} else if (NewsList.class.isAssignableFrom(rawType)) {
			return (TypeAdapter<T>) new NewListTypeAdapter(gson);
		} else {
			TypeAdapter<T> adapter = gson.getDelegateAdapter(this, tokenType);
			return new GenericTypeAdapter<T>(adapter);
		}
	}

	private static final class GenericTypeAdapter<T> extends TypeAdapter<T> {
		private final TypeAdapter<T> adapter;

		public GenericTypeAdapter(TypeAdapter<T> adapter) {
			this.adapter = adapter;
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
			adapter.write(writer, value);
		}
	}

	private static final class InstantTypeAdapter extends TypeAdapter<Instant> {

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
			if (raw != null && !raw.isEmpty()) {
				return Instant.parse(raw);
			} else {
				return null;
			}
		}

	}

	private static final class NewListTypeAdapter extends TypeAdapter<NewsList> {

		private final Gson context;

		public NewListTypeAdapter(Gson context) {
			this.context = context;
		}

		@Override
		public void write(JsonWriter out, NewsList value) throws IOException {
			TypeAdapter<News> adapter = context.getAdapter(News.class);
			out.beginArray();
			for (News n : value) {
				adapter.write(out, n);
			}
			out.endArray();
		}

		@Override
		public NewsList read(JsonReader in) throws IOException {
			if (in.peek() == JsonToken.NULL) {
				in.nextNull();
				return null;
			}

			TypeAdapter<News> adapter = context.getAdapter(News.class);
			LinkedList<News> slots = new LinkedList<>();

			in.beginArray();
			while (in.hasNext()) {
				News n = adapter.read(in);
				slots.addLast(n);
			}
			in.endArray();

			return new NewsList(slots);
		}

	}

}
