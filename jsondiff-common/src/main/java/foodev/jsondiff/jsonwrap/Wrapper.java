package foodev.jsondiff.jsonwrap;


public interface Wrapper {

    JzonElement parse(String json);


    boolean accepts(Object o);


    JzonElement wrap(Object o);


    JzonObject createJsonObject();


    JzonArray createJsonArray();

}
