package foodev.jsondiff.jsonwrap.gson;

import foodev.jsondiff.jsonwrap.JsonNull;


public class GsonJsonNull extends GsonJsonElement implements JsonNull {

    static final com.google.gson.JsonNull JNULL = new com.google.gson.JsonNull();


    public final static GsonJsonNull INSTANCE = new GsonJsonNull();


    public GsonJsonNull() {
        super(JNULL);
    }

}
