package io.wispforest.owo.config.ui;

import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.RangeConstraint;
import io.wispforest.owo.config.ui.component.ConfigSlider;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.config.ui.component.ConfigToggleButton;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.parsing.UIModel;
import net.minecraft.client.gui.widget.ButtonWidget;

import java.util.Map;
import java.util.function.Consumer;

// TODO enum cycle buttons
public class OptionComponents {

    @SuppressWarnings({"ConstantConditions"})
    public static OptionComponentFactory.Result createTextBox(UIModel model, Option<?> option, Consumer<ConfigTextBox> processor) {
        var optionComponent = model.expandTemplate(FlowLayout.class,
                "text-box-config-option",
                packParameters("text.config." + option.configName() + ".option." + option.key().asString(), option.value().toString())
        );

        var valueBox = optionComponent.childById(ConfigTextBox.class, "value-box");
        var resetButton = optionComponent.childById(ButtonWidget.class, "reset-button");
        resetButton.active = !valueBox.getText().equals(option.defaultValue().toString());
        resetButton.onPress(button -> {
            valueBox.setText(option.defaultValue().toString());
            button.active = false;
        });

        valueBox.setChangedListener(s -> resetButton.active = !s.equals(option.defaultValue().toString()));
        processor.accept(valueBox);

        return new OptionComponentFactory.Result(optionComponent, valueBox);
    }

    public static OptionComponentFactory.Result createSlider(UIModel model, Option<? extends Number> option, boolean withDecimals) {
        var value = option.value();
        var optionComponent = model.expandTemplate(FlowLayout.class,
                "range-config-option",
                packParameters("text.config." + option.configName() + ".option." + option.key().asString(), value.toString())
        );

        var constraint = option.backingField().field().getAnnotation(RangeConstraint.class);
        double min = constraint.min(), max = constraint.max();

        var valueSlider = optionComponent.childById(ConfigSlider.class, "value-slider");
        valueSlider.min(min).max(max).decimalPlaces(withDecimals ? 2 : 0).snap(!withDecimals).setFromDiscreteValue(value.doubleValue());

        var resetButton = optionComponent.childById(ButtonWidget.class, "reset-button");
        resetButton.active = (withDecimals ? value.doubleValue() : Math.round(value.doubleValue())) != option.defaultValue().doubleValue();
        resetButton.onPress(button -> {
            valueSlider.setFromDiscreteValue(option.defaultValue().doubleValue());
            button.active = false;
        });

        valueSlider.valueType(option.clazz());
        valueSlider.onChanged(newValue -> {
            resetButton.active = (withDecimals ? newValue : Math.round(newValue)) != option.defaultValue().doubleValue();
        });

        return new OptionComponentFactory.Result(optionComponent, valueSlider);
    }

    @SuppressWarnings({"ConstantConditions"})
    public static OptionComponentFactory.Result createToggleButton(UIModel model, Option<Boolean> option) {
        var optionComponent = model.expandTemplate(FlowLayout.class,
                "boolean-toggle-config-option",
                packParameters("text.config." + option.configName() + ".option." + option.key().asString(), option.value().toString())
        );

        var toggleButton = optionComponent.childById(ConfigToggleButton.class, "toggle-button");
        var resetButton = optionComponent.childById(ButtonWidget.class, "reset-button");

        resetButton.active = option.value() != option.defaultValue();
        resetButton.onPress(button -> {
            toggleButton.enabled(option.defaultValue());
            button.active = false;
        });

        toggleButton.enabled(option.value());
        toggleButton.onPress(button -> resetButton.active = toggleButton.parsedValue() != option.defaultValue());

        return new OptionComponentFactory.Result(optionComponent, toggleButton);
    }

    public static Map<String, String> packParameters(String name, String value) {
        return Map.of(
                "config-option-name", name,
                "config-option-value", value
        );
    }

}
