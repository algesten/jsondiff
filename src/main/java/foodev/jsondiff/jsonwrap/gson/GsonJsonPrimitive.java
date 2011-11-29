package foodev.jsondiff.jsonwrap.gson;

import foodev.jsondiff.jsonwrap.JsonPrimitive;


public class GsonJsonPrimitive extends GsonJsonElement implements JsonPrimitive {

    public GsonJsonPrimitive(com.google.gson.JsonPrimitive wrapped) {
        super(wrapped);
    }

}
