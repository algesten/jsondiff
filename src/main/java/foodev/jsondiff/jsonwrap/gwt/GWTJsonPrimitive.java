package foodev.jsondiff.jsonwrap.gwt;

import com.google.gwt.json.client.JSONValue;

import foodev.jsondiff.jsonwrap.JzonPrimitive;

public class GWTJsonPrimitive extends GWTJsonElement implements JzonPrimitive {

	public GWTJsonPrimitive(JSONValue wrapped) {
		super(wrapped);
	}

}
