package io.wispforest.owo.config.ui;

import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.RangeConstraint;
import io.wispforest.owo.config.ui.component.ListOptionContainer;
import io.wispforest.owo.config.ui.component.OptionComponent;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.util.NumberReflection;

import java.util.List;

public interface OptionComponentFactory<T> {

    OptionComponentFactory<? extends Number> NUMBER = (model, option) -> {
        var field = option.backingField().field();
        var floatingPoint = NumberReflection.isFloatingPointType(field.getType());

        if (field.isAnnotationPresent(RangeConstraint.class)) {
            return OptionComponents.createSlider(model, option, floatingPoint);
        } else {
            return OptionComponents.createTextBox(model, option, configTextBox -> {
                configTextBox.configureForNumber(option.clazz());
            });
        }
    };

    OptionComponentFactory<? extends CharSequence> STRING = (model, option) -> {
        return OptionComponents.createTextBox(model, option, configTextBox -> {
            if (option.constraint() != null) {
                configTextBox.applyPredicate(option.constraint()::test);
            }
        });
    };

    OptionComponentFactory<Boolean> BOOLEAN = OptionComponents::createToggleButton;

    OptionComponentFactory<? extends Enum<?>> ENUM = OptionComponents::createEnumButton;

    @SuppressWarnings({"unchecked", "rawtypes"})
    OptionComponentFactory<List<?>> LIST = (model, option) -> {
        var layout = new ListOptionContainer(option);
        return new Result(layout, layout);
    };

    Result make(UIModel model, Option<T> option);

    record Result(Component baseComponent, OptionComponent optionContainer) {}
}
