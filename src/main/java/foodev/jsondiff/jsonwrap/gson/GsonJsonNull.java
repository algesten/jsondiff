package foodev.jsondiff.jsonwrap.gson;

import com.google.gson.JsonNull;

import foodev.jsondiff.jsonwrap.JzonNull;


public class GsonJsonNull extends GsonJsonElement implements JzonNull {

    static final JsonNull JNULL = new JsonNull();


    public final static GsonJsonNull INSTANCE = new GsonJsonNull();


    public GsonJsonNull() {
        super(JNULL);
    }

}
