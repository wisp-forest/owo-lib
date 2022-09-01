package io.wispforest.owo.ui.parsing;

/**
 * Describes an error that occurred because the UIModel provided
 * to a method did not match the expectations set by said method.
 * These expectations are most often expressed in terms of component
 * classes, a violation of which will throw this exception
 */
public class IncompatibleUIModelException extends RuntimeException {

    public IncompatibleUIModelException(String message) {
        super(message);
    }

    public IncompatibleUIModelException(String message, Throwable cause) {
        super(message, cause);
    }
}
