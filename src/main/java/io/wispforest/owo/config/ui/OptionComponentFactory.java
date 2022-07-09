package io.wispforest.owo.config.ui;

import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.RangeConstraint;
import io.wispforest.owo.ui.definitions.Component;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.util.NumberReflection;

public interface OptionComponentFactory<T> {

    OptionComponentFactory<? extends Number> NUMBER = (model, option) -> {
        var field = option.backingField().field();
        boolean floatingPoint = NumberReflection.isFloatingPointType(field.getType());

        if (field.isAnnotationPresent(RangeConstraint.class)) {
            return OptionComponents.createSlider(model, option, floatingPoint);
        } else {
            return OptionComponents.createTextBox(model, option, configTextBox -> {
                configTextBox.configureForNumber(option.clazz());
            });
        }
    };

    OptionComponentFactory<? extends CharSequence> CHAR_SEQUENCE = (model, option) -> {
        return OptionComponents.createTextBox(model, option, configTextBox -> {
            if (option.constraint() != null) {
                configTextBox.applyPredicate(option.constraint()::test);
            }
        });
    };

    OptionComponentFactory<Boolean> BOOLEAN = OptionComponents::createToggleButton;

    FactoryResult make(UIModel model, Option<T> option);

    record FactoryResult(Component baseComponent, OptionComponent optionContainer) {}
}
