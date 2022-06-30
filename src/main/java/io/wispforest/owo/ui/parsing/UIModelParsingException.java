package io.wispforest.owo.ui.parsing;

public class UIModelParsingException extends RuntimeException {

    public UIModelParsingException(String message) {
        super(message);
    }

    public UIModelParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
