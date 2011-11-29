package foodev.jsondiff.jsonwrap.jackson;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import foodev.jsondiff.jsonwrap.JsonElement;
import foodev.jsondiff.jsonwrap.JsonObject;


public class JacksonJsonObject extends JacksonJsonElement implements JsonObject {

    private final org.codehaus.jackson.node.ObjectNode wrapped;


    public JacksonJsonObject(org.codehaus.jackson.node.ObjectNode wrapped) {
        super(wrapped);
        this.wrapped = wrapped;
    }


    @Override
    public boolean has(String key) {
        return wrapped.has(key);
    }


    @Override
    public JsonObject getAsJsonObject(String key) {
        return (JsonObject) JacksonWrapper.wrap(wrapped.get(key));
    }


    @Override
    public void add(String key, JsonElement tmp) {
        wrapped.put(key, (org.codehaus.jackson.JsonNode) tmp.unwrap());
    }


    @Override
    public void addProperty(String key, int prop) {
        wrapped.put(key, prop);
    }


    @Override
    public Collection<? extends Entry<String, JsonElement>> entrySet() {

        HashSet<Entry<String, JsonElement>> jset = new HashSet<Entry<String, JsonElement>>();
        
        for (Iterator<Entry<String, org.codehaus.jackson.JsonNode>> i = wrapped.getFields(); i.hasNext();) {
            
            final Entry<String, org.codehaus.jackson.JsonNode> e = i.next();
            
            final JsonElement el = JacksonWrapper.wrap(e.getValue());

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
        return JacksonWrapper.wrap(wrapped.get(key));
    }


    @Override
    public void remove(String key) {
        wrapped.remove(key);
    }

}
