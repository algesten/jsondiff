package foodev.jsondiff.jsonwrap;


public interface JsonArray extends JsonElement {

    int size();


    JsonElement get(int index);


    void addNull();


    void insert(int index, JsonElement el);


    void set(int index, JsonElement el);


    void remove(int index);

}
