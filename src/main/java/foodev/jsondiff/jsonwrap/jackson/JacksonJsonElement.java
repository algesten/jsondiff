package foodev.jsondiff.jsonwrap.jackson;

import foodev.jsondiff.jsonwrap.JsonElement;


public class JacksonJsonElement implements JsonElement {

    final org.codehaus.jackson.JsonNode wrapped;


    protected JacksonJsonElement(org.codehaus.jackson.JsonNode wrapped) {
        this.wrapped = wrapped;
    }


    @Override
    public boolean isJsonObject() {
        return wrapped.isObject();
    }


    @Override
    public boolean isJsonArray() {
        return wrapped.isArray();
    }


    @Override
    public boolean isJsonPrimitive() {
        return wrapped.isValueNode();
    }


    @Override
    public boolean isJsonNull() {
        return wrapped.isNull();
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
