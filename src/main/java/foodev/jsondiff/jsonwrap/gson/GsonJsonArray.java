package foodev.jsondiff.jsonwrap.gson;

import java.lang.reflect.Field;
import java.util.ArrayList;

import foodev.jsondiff.jsonwrap.JsonArray;
import foodev.jsondiff.jsonwrap.JsonElement;


public class GsonJsonArray extends GsonJsonElement implements JsonArray {

    private final com.google.gson.JsonArray wrapped;


    public GsonJsonArray(com.google.gson.JsonArray wrapped) {
        super(wrapped);
        this.wrapped = wrapped;
    }


    @Override
    public int size() {
        return wrapped.size();
    }


    @Override
    public JsonElement get(int index) {
        return GsonWrapper.wrap(wrapped.get(index));
    }


    @Override
    public void addNull() {
        wrapped.add(GsonJsonNull.JNULL);
    }


    @Override
    public void insert(int index, JsonElement el) {
        getElements(wrapped).add(index, (com.google.gson.JsonElement) el.unwrap());
    }


    @Override
    public void set(int index, JsonElement el) {
        getElements(wrapped).set(index, (com.google.gson.JsonElement) el.unwrap());
    }


    @Override
    public void remove(int index) {
        getElements(wrapped).remove(index);
    }


    private final static Field JsonArray_elements;


    static {

        try {
            JsonArray_elements = com.google.gson.JsonArray.class.getDeclaredField("elements");
            JsonArray_elements.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    @SuppressWarnings("unchecked")
    private static ArrayList<com.google.gson.JsonElement> getElements(com.google.gson.JsonArray arr) {

        try {
            return (ArrayList<com.google.gson.JsonElement>) JsonArray_elements.get(arr);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


}
