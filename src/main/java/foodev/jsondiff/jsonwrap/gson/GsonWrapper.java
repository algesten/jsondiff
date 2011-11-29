package foodev.jsondiff.jsonwrap.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import foodev.jsondiff.jsonwrap.JsonElement;
import foodev.jsondiff.jsonwrap.Wrapper;


public class GsonWrapper implements Wrapper {

    private final static JsonParser JSON = new JsonParser();


    public static JsonElement wrap(com.google.gson.JsonElement el) {
        if (el == null || el.isJsonNull()) {
            return GsonJsonNull.INSTANCE;
        } else if (el.isJsonArray()) {
            return new GsonJsonArray((JsonArray) el);
        } else if (el.isJsonObject()) {
            return new GsonJsonObject((JsonObject) el);
        } else if (el.isJsonPrimitive()) {
            return new GsonJsonPrimitive((JsonPrimitive) el);
        } else {
            throw new IllegalStateException();
        }
    }


    @Override
    public JsonElement parse(String json) {
        return wrap(JSON.parse(json));
    }


    @Override
    public boolean accepts(Object o) {
        return o == null || o instanceof GsonJsonElement || o instanceof com.google.gson.JsonElement;
    }


    @Override
    public JsonElement wrap(Object o) {
        return wrap((com.google.gson.JsonElement) o);
    }


    @Override
    public foodev.jsondiff.jsonwrap.JsonObject createJsonObject() {
        return (foodev.jsondiff.jsonwrap.JsonObject) wrap(new JsonObject());
    }


    @Override
    public foodev.jsondiff.jsonwrap.JsonArray createJsonArray() {
        return (foodev.jsondiff.jsonwrap.JsonArray) wrap(new JsonArray());
    }


}
