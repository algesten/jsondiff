package foodev.jsondiff.jsonwrap.gwt;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

import foodev.jsondiff.jsonwrap.JzonArray;
import foodev.jsondiff.jsonwrap.JzonElement;
import foodev.jsondiff.jsonwrap.JzonObject;
import foodev.jsondiff.jsonwrap.Wrapper;

public class GWTWrapper implements Wrapper {

	public static JzonElement wrap(JSONValue el) {
		if (el == null || el.isNull() != null) {
			return GWTJsonNull.INSTANCE;
		} else if (el.isArray() != null) {
			return new GWTJsonArray(el.isArray());
		} else if (el.isObject() != null) {
			return new GWTJsonObject(el.isObject());
		} else if (el.isNumber() != null) {
			return new GWTJsonPrimitive(el.isNumber());
		} else if (el.isBoolean() != null) {
			return new GWTJsonPrimitive(el.isBoolean());
		} else if (el.isString() != null) {
			return new GWTJsonPrimitive(el.isString());
		} else {
			throw new IllegalStateException();
		}
	}

	@Override
	public JzonElement parse(String json) {
		return wrap(JSONParser.parseLenient(json));
	}

	@Override
	public boolean accepts(Object o) {
		return o instanceof GWTJsonElement || o instanceof JSONValue;
	}

	@Override
	public JzonElement wrap(Object o) {
		if (o instanceof JzonElement) {
			return (JzonElement) o;
		}
		return wrap((JSONValue) o);
	}

	@Override
	public JzonObject createJsonObject() {
		return (JzonObject) wrap(new JSONObject());
	}

	@Override
	public JzonArray createJsonArray() {
		return (JzonArray) wrap(new JSONArray());
	}

}
