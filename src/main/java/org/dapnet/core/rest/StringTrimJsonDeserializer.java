package org.dapnet.core.rest;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * Custom JSON deserializer for strings that trims whitespaces.
 * 
 * @author Philipp Thiel
 */
public class StringTrimJsonDeserializer implements JsonDeserializer<String> {

	@Override
	public String deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		String text = json.getAsString();
		return text != null ? text.trim() : null;
	}

}
