package io.wispforest.owo.config.ui;

import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.RangeConstraint;
import io.wispforest.owo.config.ui.component.ListOptionContainer;
import io.wispforest.owo.config.ui.component.OptionComponent;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.util.NumberReflection;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * A function which creates an instance of {@link OptionComponent}
 * fitting for the given config option. Whatever component is created
 * should accurately reflect if the option is currently detached
 * and thus immutable - ideally it is non-interactable
 *
 * @param <T> The type of option for which this factory can create components
 */
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

    OptionComponentFactory<Identifier> IDENTIFIER = (model, option) -> {
        return OptionComponents.createTextBox(model, option, configTextBox -> {
            configTextBox.inputPredicate(s -> s.matches("[a-z0-9_.:\\-]*"));
            configTextBox.applyPredicate(s -> Identifier.tryParse(s) != null);
            configTextBox.valueParser(Identifier::new);
        });
    };

    OptionComponentFactory<Boolean> BOOLEAN = OptionComponents::createToggleButton;

    OptionComponentFactory<? extends Enum<?>> ENUM = OptionComponents::createEnumButton;

    @SuppressWarnings({"unchecked", "rawtypes"})
    OptionComponentFactory<List<?>> LIST = (model, option) -> {
        var layout = new ListOptionContainer(option);
        return new Result(layout, layout);
    };

    /**
     * Create a new component fitting for, and bound to,
     * the given config option
     *
     * @param model  The UI model of the enclosing screen, used
     *               for expanding templates
     * @param option The option for which to create a component
     * @return The option component as well as a potential wrapping
     * component, this simply be the option component itself
     */
    Result make(UIModel model, Option<T> option);

    record Result(Component baseComponent, OptionComponent optionContainer) {}
}
