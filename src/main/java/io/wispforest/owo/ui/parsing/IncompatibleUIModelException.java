package io.wispforest.owo.ui.parsing;

public class IncompatibleUIModelException extends RuntimeException {

    public IncompatibleUIModelException(String message) {
        super(message);
    }

    public IncompatibleUIModelException(String message, Throwable cause) {
        super(message, cause);
    }
}
