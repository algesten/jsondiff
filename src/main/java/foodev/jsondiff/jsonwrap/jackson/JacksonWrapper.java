package foodev.jsondiff.jsonwrap.jackson;


import java.io.IOException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.ValueNode;

import foodev.jsondiff.jsonwrap.JsonElement;
import foodev.jsondiff.jsonwrap.JsonWrapperException;
import foodev.jsondiff.jsonwrap.Wrapper;


public class JacksonWrapper implements Wrapper {

    private final static ObjectMapper JSON = new ObjectMapper();


    public static JsonElement wrap(JsonNode el) {
        if (el == null || el.isNull()) {
            return JacksonJsonNull.INSTANCE;
        } else if (el.isArray()) {
            return new JacksonJsonArray((ArrayNode) el);
        } else if (el.isObject()) {
            return new JacksonJsonObject((ObjectNode) el);
        } else if (el.isValueNode()) {
            return new JacksonJsonPrimitive((ValueNode) el);
        } else {
            throw new IllegalStateException();
        }
    }


    @Override
    public JsonElement parse(String json) {
        try {
            return wrap(JSON.readTree(json));
        } catch (JsonProcessingException e) {
            throw new JsonWrapperException("Failed to parse JSON", e);
        } catch (IOException e) {
            throw new JsonWrapperException("IOException parsing a String?", e);
        }
    }


    @Override
    public boolean accepts(Object o) {
        return o == null || o instanceof JacksonJsonElement || o instanceof org.codehaus.jackson.JsonNode;
    }


    @Override
    public JsonElement wrap(Object o) {
        return wrap((JsonNode) o);
    }


    @Override
    public foodev.jsondiff.jsonwrap.JsonObject createJsonObject() {
        return (foodev.jsondiff.jsonwrap.JsonObject) wrap(JSON.createObjectNode());
    }


    @Override
    public foodev.jsondiff.jsonwrap.JsonArray createJsonArray() {
        return (foodev.jsondiff.jsonwrap.JsonArray) wrap(JSON.createArrayNode());
    }


}
