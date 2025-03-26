package io.wispforest.owo.config.ui;

import io.wispforest.owo.config.ui.component.OptionValueProvider;
import io.wispforest.owo.ui.core.Component;

import java.util.function.Supplier;

public record OptionComponentData<B extends Component, P extends OptionValueProvider>(B baseComponent, P optionProvider,
                                                                                      Supplier<String> searchTextSource) {
}
