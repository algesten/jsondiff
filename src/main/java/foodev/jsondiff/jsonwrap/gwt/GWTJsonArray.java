package foodev.jsondiff.jsonwrap.gwt;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONValue;

import foodev.jsondiff.jsonwrap.JzonArray;
import foodev.jsondiff.jsonwrap.JzonElement;

public class GWTJsonArray extends GWTJsonElement implements JzonArray {

	private JSONArray wrapped;

	public static native JavaScriptObject remove(JavaScriptObject array, int index, int howMany)
	/*-{
		return array.splice(index, howMany);
	}-*/;

	public static native JavaScriptObject insert(JavaScriptObject array, int index, JSONValue value)
	/*-{
		if (value) {
			var func = value.@com.google.gwt.json.client.JSONValue::getUnwrapper()();
			value = func(value);
		} else {
			// Coerce Java null to undefined; there's a JSONNull for null.
			value = undefined;
		}
		return array.splice(index, 0, value);
	}-*/;

	public GWTJsonArray(JSONArray wrapped) {
		super(wrapped);
		this.wrapped = wrapped;
	}

	@Override
	public int size() {
		return wrapped.size();
	}

	@Override
	public JzonElement get(int index) {
		return GWTWrapper.wrap(wrapped.get(index));
	}

	@Override
	public void addNull() {
		wrapped.set(wrapped.size(), JSONNull.getInstance());
	}

	@Override
	public void insert(int index, JzonElement el) {
		JavaScriptObject newArray = insert(wrapped.getJavaScriptObject(), index, (JSONValue) el.unwrap());
		wrapped = new JSONArray(newArray);
	}

	@Override
	public void set(int index, JzonElement el) {
		wrapped.set(index, (JSONValue) el.unwrap());
	}

	@Override
	public void remove(int index) {
		JavaScriptObject newArray = remove(wrapped.getJavaScriptObject(), index, 1);
		wrapped = new JSONArray(newArray);
	}

	@Override
	public String toString() {
		return wrapped.toString();
	}

}
