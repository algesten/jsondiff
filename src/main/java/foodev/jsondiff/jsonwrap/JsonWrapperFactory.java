package foodev.jsondiff.jsonwrap;

import foodev.jsondiff.jsonwrap.gson.GsonWrapper;
import foodev.jsondiff.jsonwrap.jackson.JacksonWrapper;


/**
 * Internal wrapper helper.
 * 
 * @author Martin Algesten
 * 
 */
public class JsonWrapperFactory {

    private final static Wrapper gsonWrapper = buildGsonWrapper();
    
    private final static Wrapper jacksonWrapper = buildJacksonWrapper();

    public static JzonElement parse(String json, Object hint) {
        return selectWrapper(hint).parse(json);
    }


    public static JzonObject createJsonObject(Object hint) {
        return selectWrapper(hint).createJsonObject();
    }


    public static JzonArray createJsonArray(Object hint) {
        return selectWrapper(hint).createJsonArray();
    }


    public static JzonElement wrap(Object obj) {
        if (obj instanceof JzonElement) {
            return (JzonElement) obj;
        } else {
            return selectWrapper(obj).wrap(obj);
        }
    }


    private static Wrapper selectWrapper(Object hint) {
        if (gsonWrapper != null && (hint == null || gsonWrapper.accepts(hint))) {
            return gsonWrapper;
        } else if (jacksonWrapper != null && (hint == null || jacksonWrapper.accepts(hint))) {
            return jacksonWrapper;
        } else {
            throw new IllegalStateException("No json wrapper accepts: " + (hint == null ? hint : hint.getClass().getName()));
        }
    }


    private static Wrapper buildGsonWrapper() {
        try {
            Class.forName("com.google.gson.JsonElement");
            return new GsonWrapper();
        } catch (ClassNotFoundException cnfe) {
            return null;
        }
    }

    private static Wrapper buildJacksonWrapper() {
        try {
            Class.forName("org.codehaus.jackson.JsonNode");
            return new JacksonWrapper();
        } catch (ClassNotFoundException cnfe) {
            return null;
        }
    }

}
