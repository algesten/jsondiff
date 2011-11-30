package foodev.jsondiff.jsonwrap.jackson;

import org.codehaus.jackson.node.ValueNode;

import foodev.jsondiff.jsonwrap.JzonPrimitive;


public class JacksonJsonPrimitive extends JacksonJsonElement implements JzonPrimitive {

    public JacksonJsonPrimitive(ValueNode wrapped) {
        super(wrapped);
    }

}
