package foodev.jsondiff.jsonwrap;


public interface Wrapper {

    JsonElement parse(String json);


    boolean accepts(Object o);


    JsonElement wrap(Object o);


    JsonObject createJsonObject();


    JsonArray createJsonArray();

}
