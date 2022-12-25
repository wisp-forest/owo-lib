package io.wispforest.owo.config.ui;

import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.RangeConstraint;
import io.wispforest.owo.config.ui.component.*;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.parsing.UIModel;

import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings("ConstantConditions")
public class OptionComponents {

    public static OptionComponentFactory.Result createTextBox(UIModel model, Option<?> option, Consumer<ConfigTextBox> processor) {
        var optionComponent = model.expandTemplate(FlowLayout.class,
                "text-box-config-option",
                packParameters(option.translationKey(), option.value().toString())
        );

        var valueBox = optionComponent.childById(ConfigTextBox.class, "value-box");
        var resetButton = optionComponent.childById(ButtonComponent.class, "reset-button");

        if (option.detached()) {
            resetButton.active = false;
            valueBox.setEditable(false);
        } else {
            resetButton.active = !valueBox.getText().equals(option.defaultValue().toString());
            resetButton.onPress(button -> {
                valueBox.setText(option.defaultValue().toString());
                button.active = false;
            });

            valueBox.onChanged().subscribe(s -> resetButton.active = !s.equals(option.defaultValue().toString()));
        }

        processor.accept(valueBox);

        optionComponent.child(new SearchAnchorComponent(
                optionComponent,
                option.key(),
                () -> optionComponent.childById(LabelComponent.class, "option-name").text().getString(),
                valueBox::getText
        ));

        return new OptionComponentFactory.Result(optionComponent, valueBox);
    }

    /**
     * @deprecated Use {@link #createSlider} instead
     */
    @Deprecated(forRemoval = true)
    public static OptionComponentFactory.Result createSlider(UIModel model, Option<? extends Number> option, boolean withDecimals) {
        return createSlider(model, option, withDecimals ? 2 : 0);
    }

    public static OptionComponentFactory.Result createSlider(UIModel model, Option<? extends Number> option, int decimalPlaces) {
        boolean withDecimals = decimalPlaces > 0;

        var value = option.value();
        var optionComponent = model.expandTemplate(FlowLayout.class,
                "range-config-option",
                packParameters(option.translationKey(), value.toString())
        );

        var constraint = option.backingField().field().getAnnotation(RangeConstraint.class);
        double min = constraint.min(), max = constraint.max();

        var valueSlider = optionComponent.childById(ConfigSlider.class, "value-slider");
        valueSlider.min(min).max(max).decimalPlaces(decimalPlaces).snap(!withDecimals).setFromDiscreteValue(value.doubleValue());
        valueSlider.valueType(option.clazz());

        var resetButton = optionComponent.childById(ButtonComponent.class, "reset-button");

        if (option.detached()) {
            resetButton.active = false;
            valueSlider.active = false;
        } else {
            resetButton.active = (withDecimals ? value.doubleValue() : Math.round(value.doubleValue())) != option.defaultValue().doubleValue();
            resetButton.onPress(button -> {
                valueSlider.setFromDiscreteValue(option.defaultValue().doubleValue());
                button.active = false;
            });

            valueSlider.onChanged().subscribe(newValue -> {
                resetButton.active = (withDecimals ? newValue : Math.round(newValue)) != option.defaultValue().doubleValue();
            });
        }

        optionComponent.child(new SearchAnchorComponent(
                optionComponent,
                option.key(),
                () -> optionComponent.childById(LabelComponent.class, "option-name").text().getString(),
                () -> valueSlider.getMessage().getString()
        ));

        return new OptionComponentFactory.Result(optionComponent, valueSlider);
    }

    public static OptionComponentFactory.Result createToggleButton(UIModel model, Option<Boolean> option) {
        var optionComponent = model.expandTemplate(FlowLayout.class,
                "boolean-toggle-config-option",
                packParameters(option.translationKey(), option.value().toString())
        );

        var toggleButton = optionComponent.childById(ConfigToggleButton.class, "toggle-button");
        var resetButton = optionComponent.childById(ButtonComponent.class, "reset-button");

        toggleButton.enabled(option.value());

        if (option.detached()) {
            resetButton.active = false;
            toggleButton.active = false;
        } else {
            resetButton.active = option.value() != option.defaultValue();
            resetButton.onPress(button -> {
                toggleButton.enabled(option.defaultValue());
                button.active = false;
            });

            toggleButton.onPress(button -> resetButton.active = toggleButton.parsedValue() != option.defaultValue());
        }

        optionComponent.child(new SearchAnchorComponent(
                optionComponent,
                option.key(),
                () -> optionComponent.childById(LabelComponent.class, "option-name").text().getString(),
                () -> toggleButton.getMessage().getString()
        ));

        return new OptionComponentFactory.Result(optionComponent, toggleButton);
    }

    public static OptionComponentFactory.Result createEnumButton(UIModel model, Option<? extends Enum<?>> option) {
        var optionComponent = model.expandTemplate(FlowLayout.class,
                "enum-config-option",
                packParameters(option.translationKey(), option.value().toString())
        );

        var enumButton = optionComponent.childById(ConfigEnumButton.class, "enum-button");
        var resetButton = optionComponent.childById(ButtonComponent.class, "reset-button");

        enumButton.init(option, option.value().ordinal());

        if (option.detached()) {
            resetButton.active = false;
            enumButton.active = false;
        } else {
            resetButton.active = option.value() != option.defaultValue();
            resetButton.onPress(button -> {
                enumButton.select(option.defaultValue().ordinal());
                button.active = false;
            });

            enumButton.onPress(button -> resetButton.active = enumButton.parsedValue() != option.defaultValue());
        }

        optionComponent.child(new SearchAnchorComponent(
                optionComponent,
                option.key(),
                () -> optionComponent.childById(LabelComponent.class, "option-name").text().getString(),
                () -> enumButton.getMessage().getString()
        ));

        return new OptionComponentFactory.Result(optionComponent, enumButton);
    }

    public static Map<String, String> packParameters(String name, String value) {
        return Map.of(
                "config-option-name", name,
                "config-option-value", value
        );
    }

}
