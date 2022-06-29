package io.wispforest.owo.ui.parse;

public class UIParsingException extends RuntimeException {

    public UIParsingException(String message) {
        super(message);
    }

    public UIParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
