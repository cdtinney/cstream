package com.cstream.util;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.logging.Logger;

import com.cstream.logging.LogLevel;
import com.cstream.model.Song;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

public class JsonUtils {

	private static Logger LOGGER = Logger.getLogger(JsonUtils.class.getName());
	
	private static Gson gson = new Gson();
	
	public static Map<String, Song> parseJsonSongMap(String jString) {
		
		try {			
			Type type = new TypeToken<Map<String, Song>>(){}.getType();
			
			Gson gson = new Gson();
			return gson.fromJson(jString, type);

		} catch (JsonParseException e) {
			LOGGER.warning("JSON parse error: " + e.getMessage());
			LOGGER.log(LogLevel.DEBUG, "Could not parse: " + jString);
			
		}
		
		return null;
		
	}
	
	public static Map<String, String> parseJsonStringMap(String jString) {
		
		try {
			Type type = new TypeToken<Map<String, String>>(){}.getType();
			return gson.fromJson(jString, type);

		} catch (JsonParseException e) {
			LOGGER.log(LogLevel.DEBUG, "Could not parse: " + jString);
			
		}

		return null;
		
	}
	
	public static String toJson(String property, String value) {
		
		JsonObject object = new JsonObject();
		object.add(property, new JsonPrimitive(value));
		return object.toString();
		
	}
	
	public static String getJson(Object obj) {
		return gson.toJson(obj);
	}

}
