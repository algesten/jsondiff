package foodev.jsondiff.jsonwrap;


public class JsonWrapperException extends RuntimeException {

    private static final long serialVersionUID = 1186398877735507475L;

    public JsonWrapperException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonWrapperException(String message) {
        super(message);
    }
    
}
