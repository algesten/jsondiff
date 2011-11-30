package foodev.jsondiff.jsonwrap;


/**
 * Indicates a JSON parsing exception.
 * 
 * @author Dan Everton
 * 
 */
public class JsonWrapperException extends RuntimeException {

    private static final long serialVersionUID = 1186398877735507475L;


    /**
     * Constructs setting message and cause.
     * 
     * @param message
     * @param cause
     */
    public JsonWrapperException(String message, Throwable cause) {
        super(message, cause);
    }


    /**
     * Constructs setting message.
     * 
     * @param message
     */
    public JsonWrapperException(String message) {
        super(message);
    }

}
