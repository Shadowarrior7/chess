package server;

public class GenericException extends RuntimeException {
    public int code;
    public GenericException(String message, int code) {
        super(message);
        code = this.code;
    }
}
