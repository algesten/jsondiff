package foodev.jsondiff.jsonwrap;

import foodev.jsondiff.jsonwrap.gson.GsonWrapper;


public class JsonWrapperFactory {

    private final static Wrapper gsonWrapper = new GsonWrapper();
    private final static Wrapper jacksonWrapper;


    public static JsonElement parse(String json, Object hint) {
        return selectWrapper(hint).parse(json);
    }


    public static JsonObject createJsonObject(Object hint) {
        return selectWrapper(hint).createJsonObject();
    }


    public static JsonArray createJsonArray(Object hint) {
        return selectWrapper(hint).createJsonArray();
    }


    public static JsonElement wrap(Object obj) {
        if (obj instanceof JsonElement) {
            return (JsonElement) obj;
        } else {
            return selectWrapper(obj).wrap(obj);
        }
    }


    private static Wrapper selectWrapper(Object hint) {
        if (hint == null || gsonWrapper.accepts(hint)) {
            return gsonWrapper;
        } else if (jacksonWrapper != null && jacksonWrapper.accepts(hint)) {
            return jacksonWrapper;
        } else {
            throw new IllegalStateException("No json wrapper accepts: " + hint.getClass().getName());
        }
    }


    static {

        boolean hasJackson = false;
        Wrapper maybe = null;

        try {
            Class.forName("org.codehaus.jackson.JsonNode");
            hasJackson = true;
        } catch (ClassNotFoundException cnfe) {
            hasJackson = false;
        }

        if (hasJackson) {
            try {
                maybe = (Wrapper)
                        Class.forName("foodev.jsondiff.jsonwrap.jackson.JacksonWrapper").newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        jacksonWrapper = maybe;

    }
}
