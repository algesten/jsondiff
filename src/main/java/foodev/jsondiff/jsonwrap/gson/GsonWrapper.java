package foodev.jsondiff.jsonwrap.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import foodev.jsondiff.jsonwrap.JzonElement;
import foodev.jsondiff.jsonwrap.Wrapper;

public class GsonWrapper implements Wrapper {

	private final static JsonParser JSON = new JsonParser();

	public static JzonElement wrap(JsonElement el) {
		if (el == null || el.isJsonNull()) {
			return GsonJsonNull.INSTANCE;
		} else if (el.isJsonArray()) {
			return new GsonJsonArray((JsonArray) el);
		} else if (el.isJsonObject()) {
			return new GsonJsonObject((JsonObject) el);
		} else if (el.isJsonPrimitive()) {
			return new GsonJsonPrimitive((JsonPrimitive) el);
		} else {
			throw new IllegalStateException();
		}
	}

	@Override
	public JzonElement parse(String json) {
		return wrap(JSON.parse(json));
	}

	@Override
	public JzonElement wrap(Object o) {
		return wrap((JsonElement) o);
	}

	@Override
	public foodev.jsondiff.jsonwrap.JzonObject createJsonObject() {
		return (foodev.jsondiff.jsonwrap.JzonObject) wrap(new JsonObject());
	}

	@Override
	public foodev.jsondiff.jsonwrap.JzonArray createJsonArray() {
		return (foodev.jsondiff.jsonwrap.JzonArray) wrap(new JsonArray());
	}

}
