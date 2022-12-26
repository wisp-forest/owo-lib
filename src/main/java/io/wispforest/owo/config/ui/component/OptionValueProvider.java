package io.wispforest.owo.config.ui.component;

public interface OptionValueProvider {

    /**
     * @return {@code true} if the current state of this component
     * describes a valid value for the option it is linked to
     */
    boolean isValid();

    /**
     * @return The value described by the current state
     * of this component
     */
    Object parsedValue();
}
