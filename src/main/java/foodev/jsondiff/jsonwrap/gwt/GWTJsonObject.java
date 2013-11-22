package foodev.jsondiff.jsonwrap.gwt;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;

import foodev.jsondiff.jsonwrap.JzonElement;
import foodev.jsondiff.jsonwrap.JzonObject;

public class GWTJsonObject extends GWTJsonElement implements JzonObject {

	public static native void unset(JavaScriptObject object, String key)
	/*-{
	  delete object[key];
	}-*/;

	private final JSONObject wrapped;

	public GWTJsonObject(JSONObject wrapped) {
		super(wrapped);
		this.wrapped = wrapped;
	}

	@Override
	public void add(String key, JzonElement prop) {
		wrapped.put(key, (JSONValue) prop.unwrap());
	}

	@Override
	public void addProperty(String key, int prop) {
		if (key.startsWith("-0")) {
			//gwt bug
			key = "--0";
		}
		wrapped.put(key, new JSONNumber(prop));
	}

	@Override
	public Collection<? extends Entry<String, JzonElement>> entrySet() {

		HashSet<Entry<String, JzonElement>> jset = new HashSet<Entry<String, JzonElement>>();

		for (final String key : wrapped.keySet()) {

			final JzonElement el = GWTWrapper.wrap(wrapped.get(key));

			jset.add(new Entry<String, JzonElement>() {

				@Override
				public String getKey() {
					return key;
				}

				@Override
				public JzonElement getValue() {
					return el;
				}

				@Override
				public JzonElement setValue(JzonElement value) {
					throw new UnsupportedOperationException();
				}
			});
		}

		return jset;

	}

	@Override
	public JzonElement get(String key) {
		return GWTWrapper.wrap(wrapped.get(key));
	}

	@Override
	public boolean has(String key) {
		return wrapped.containsKey(key);
	}

	@Override
	public void remove(String key) {
		unset(wrapped.getJavaScriptObject(), key);
	}

	@Override
	public String toString() {
		return wrapped.toString();
	}

}
