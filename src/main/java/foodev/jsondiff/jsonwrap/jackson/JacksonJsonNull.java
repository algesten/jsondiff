package foodev.jsondiff.jsonwrap.jackson;

import foodev.jsondiff.jsonwrap.JsonNull;


public class JacksonJsonNull extends JacksonJsonElement implements JsonNull {

    static final org.codehaus.jackson.node.NullNode JNULL = org.codehaus.jackson.node.NullNode.getInstance();


    public final static JacksonJsonNull INSTANCE = new JacksonJsonNull();


    public JacksonJsonNull() {
        super(JNULL);
    }

}
