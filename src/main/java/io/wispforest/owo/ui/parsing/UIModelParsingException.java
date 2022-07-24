package io.wispforest.owo.ui.parsing;

/**
 * Describes an error that happened during instantiation
 * of a UIModel, most commonly due to improperly formatted XML
 * or XML which describes invalid values
 */
public class UIModelParsingException extends RuntimeException {

    public UIModelParsingException(String message) {
        super(message);
    }

    public UIModelParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
