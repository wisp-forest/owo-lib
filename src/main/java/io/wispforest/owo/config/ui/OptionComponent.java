package io.wispforest.owo.config.ui;

import io.wispforest.owo.ui.core.Component;

public interface OptionComponent extends Component {

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
