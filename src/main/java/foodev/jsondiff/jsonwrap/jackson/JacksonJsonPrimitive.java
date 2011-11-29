package foodev.jsondiff.jsonwrap.jackson;

import foodev.jsondiff.jsonwrap.JsonPrimitive;


public class JacksonJsonPrimitive extends JacksonJsonElement implements JsonPrimitive {

    public JacksonJsonPrimitive(org.codehaus.jackson.node.ValueNode wrapped) {
        super(wrapped);
    }

}
