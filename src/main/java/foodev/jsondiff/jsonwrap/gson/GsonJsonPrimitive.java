package foodev.jsondiff.jsonwrap.gson;

import com.google.gson.JsonPrimitive;

import foodev.jsondiff.jsonwrap.JzonPrimitive;


public class GsonJsonPrimitive extends GsonJsonElement implements JzonPrimitive {

    public GsonJsonPrimitive(JsonPrimitive wrapped) {
        super(wrapped);
    }

}
