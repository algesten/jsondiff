package foodev.jsondiff.jsonwrap;


public interface JzonElement {

    boolean isJsonObject();


    boolean isJsonArray();


    boolean isJsonPrimitive();


    boolean isJsonNull();


    Object unwrap();

}
