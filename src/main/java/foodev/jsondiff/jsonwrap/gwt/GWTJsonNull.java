package foodev.jsondiff.jsonwrap.gwt;

import com.google.gwt.json.client.JSONNull;

import foodev.jsondiff.jsonwrap.JzonNull;

public class GWTJsonNull extends GWTJsonElement implements JzonNull {

	public final static GWTJsonNull INSTANCE = new GWTJsonNull();

	public GWTJsonNull() {
		super(JSONNull.getInstance());
	}

}
