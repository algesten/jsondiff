package foodev.jsondiff;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import foodev.jsondiff.jsonwrap.gson.GsonWrapper;

public class GsonDiff extends JsonDiff {

	public GsonDiff() {
		super(new GsonWrapper());
	}

	public JsonObject diff(JsonElement from, JsonElement to) throws IllegalArgumentException {
		return (JsonObject) super.diff(from, to);
	}
}
