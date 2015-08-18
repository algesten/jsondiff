package foodev.jsondiff.jsonwrap.jackson2;

import com.fasterxml.jackson.databind.node.ValueNode;
import foodev.jsondiff.jsonwrap.JzonPrimitive;


public class Jackson2JsonPrimitive extends Jackson2JsonElement implements JzonPrimitive {

    public Jackson2JsonPrimitive(ValueNode wrapped) {
        super(wrapped);
    }

}
