package foodev.jsondiff.jsonwrap;


public interface JsonElement {

    boolean isJsonObject();


    boolean isJsonArray();


    boolean isJsonPrimitive();


    boolean isJsonNull();


    Object unwrap();

}
