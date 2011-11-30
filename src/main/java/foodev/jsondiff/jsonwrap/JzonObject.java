package foodev.jsondiff.jsonwrap;

import java.util.Collection;
import java.util.Map.Entry;


public interface JzonObject extends JzonElement {


    boolean has(String key);


    JzonObject getAsJsonObject(String key);


    void add(String key, JzonElement tmp);


    void addProperty(String key, int prop);


    Collection<? extends Entry<String, JzonElement>> entrySet();


    JzonElement get(String key);


    void remove(String key);

}
