package foodev.jsondiff.jsonwrap.gson;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import foodev.jsondiff.jsonwrap.JsonElement;
import foodev.jsondiff.jsonwrap.JsonObject;


public class GsonJsonObject extends GsonJsonElement implements JsonObject {

    private final com.google.gson.JsonObject wrapped;


    public GsonJsonObject(com.google.gson.JsonObject wrapped) {
        super(wrapped);
        this.wrapped = wrapped;
    }


    @Override
    public boolean has(String key) {
        return wrapped.has(key);
    }


    @Override
    public JsonObject getAsJsonObject(String key) {
        return (JsonObject) GsonWrapper.wrap(wrapped.getAsJsonObject(key));
    }


    @Override
    public void add(String key, JsonElement tmp) {
        wrapped.add(key, (com.google.gson.JsonElement)tmp.unwrap());
    }


    @Override
    public void addProperty(String key, int prop) {
        wrapped.addProperty(key, prop);
    }


    @Override
    public Collection<? extends Entry<String, JsonElement>> entrySet() {

        Set<Entry<String, com.google.gson.JsonElement>> set = wrapped.entrySet();

        HashSet<Entry<String, JsonElement>> jset = new HashSet<Entry<String, JsonElement>>();

        for (final Entry<String, com.google.gson.JsonElement> e : set) {

            final JsonElement el = GsonWrapper.wrap(e.getValue());

            jset.add(new Entry<String, JsonElement>() {

                @Override
                public String getKey() {
                    return e.getKey();
                }


                @Override
                public JsonElement getValue() {
                    return el;
                }


                @Override
                public JsonElement setValue(JsonElement value) {
                    throw new UnsupportedOperationException();
                }
            });
        }

        return jset;

    }


    @Override
    public JsonElement get(String key) {
        return GsonWrapper.wrap(wrapped.get(key));
    }


    @Override
    public void remove(String key) {
        wrapped.remove(key);
    }

}
