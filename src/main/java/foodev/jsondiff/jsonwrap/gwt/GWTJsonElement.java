package foodev.jsondiff.jsonwrap.gwt;

import com.google.gwt.json.client.JSONValue;

import foodev.jsondiff.jsonwrap.JzonElement;

public class GWTJsonElement implements JzonElement {

	final JSONValue wrapped;

	protected GWTJsonElement(JSONValue wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public boolean isJsonObject() {
		return wrapped.isObject() != null;
	}

	@Override
	public boolean isJsonArray() {
		return wrapped.isArray() != null;
	}

	@Override
	public boolean isJsonPrimitive() {
		return wrapped.isBoolean() != null || wrapped.isNumber() != null || wrapped.isString() != null;
	}

	@Override
	public boolean isJsonNull() {
		return wrapped.isNull() != null;
	}

	@Override
	public Object unwrap() {
		return wrapped;
	}

	@Override
	public String toString() {
		return wrapped.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return wrapped.equals(obj);
	}

	@Override
	public int hashCode() {
		return wrapped.hashCode();
	}

}
