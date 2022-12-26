package io.wispforest.owo.config.ui.component;

import io.wispforest.owo.ui.core.Component;

/**
 * @deprecated Implement {@link OptionValueProvider} instead.
 * There is no longer any requirement for implementors to be UI
 * components themselves
 */
@Deprecated(forRemoval = true)
public interface OptionComponent extends OptionValueProvider, Component {}
