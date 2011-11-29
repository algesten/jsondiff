package foodev.jsondiff.jsonwrap.jackson;


import foodev.jsondiff.jsonwrap.JsonArray;
import foodev.jsondiff.jsonwrap.JsonElement;


public class JacksonJsonArray extends JacksonJsonElement implements JsonArray {

    private final org.codehaus.jackson.node.ArrayNode wrapped;


    public JacksonJsonArray(org.codehaus.jackson.node.ArrayNode wrapped) {
        super(wrapped);
        this.wrapped = wrapped;
    }


    @Override
    public int size() {
        return wrapped.size();
    }


    @Override
    public JsonElement get(int index) {
        return JacksonWrapper.wrap(wrapped.get(index));
    }


    @Override
    public void addNull() {
        wrapped.add(JacksonJsonNull.JNULL);
    }


    @Override
    public void insert(int index, JsonElement el) {
        wrapped.insert(index,  (org.codehaus.jackson.JsonNode) el.unwrap());
    }


    @Override
    public void set(int index, JsonElement el) {
        wrapped.set(index,  (org.codehaus.jackson.JsonNode) el.unwrap());
    }


    @Override
    public void remove(int index) {
        wrapped.remove(index);
    }


}
