package foodev.jsondiff.jsonwrap;

import java.util.Collection;
import java.util.Map.Entry;


public interface JsonObject extends JsonElement {


    boolean has(String key);


    JsonObject getAsJsonObject(String key);


    void add(String key, JsonElement tmp);


    void addProperty(String key, int prop);


    Collection<? extends Entry<String, JsonElement>> entrySet();


    JsonElement get(String key);


    void remove(String key);

}
