package server;

import java.io.Serializable;

public class GenericException extends RuntimeException implements Serializable {
    public int code;

    public GenericException(String message, int code) {
        super(message);
        this.code = code;
    }
    public int getcode(){
        return code;
    }
}
