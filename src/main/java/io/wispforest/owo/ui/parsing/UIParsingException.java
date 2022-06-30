package io.wispforest.owo.ui.parsing;

public class UIParsingException extends RuntimeException {

    public UIParsingException(String message) {
        super(message);
    }

    public UIParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
