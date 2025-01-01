package me.eldodebug.jsmp.exceptions;

public class JsmpException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public JsmpException(String message) {
        super(message);
    }

    public JsmpException(String message, Throwable cause) {
        super(message, cause);
    }
}