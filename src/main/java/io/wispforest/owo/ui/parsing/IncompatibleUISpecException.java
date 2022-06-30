package io.wispforest.owo.ui.parsing;

public class IncompatibleUISpecException extends RuntimeException {

    public IncompatibleUISpecException(String message) {
        super(message);
    }

    public IncompatibleUISpecException(String message, Throwable cause) {
        super(message, cause);
    }
}
