package io.wispforest.owo.config.ui;

import io.wispforest.owo.config.options.OptionControlSpec;
import io.wispforest.owo.config.ui.component.*;
import io.wispforest.owo.ui.component.*;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.parsing.UIModel;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.mutable.MutableBoolean;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.wispforest.owo.config.ui.OptionComponentFactory.Result;

// TODO: [Config Rewrite] CHANGE USE OF Option TO SOME ABSTRACTED INTERFACE
@SuppressWarnings("ConstantConditions")
public class OptionComponents {

    public static Result<FlowLayout, ConfigTextBox> createStringComponent(UIModel model, OptionControlSpec<CharSequence> option, boolean isDetached) {
        return OptionComponents.createTextBox(model, option, configTextBox -> {
            if (option.constraint() != null) {
                configTextBox.applyPredicate(option.constraint()::test);
            }
        }, isDetached);
    }

    public static Result<FlowLayout, ConfigTextBox> createIdentifierComponent(UIModel model, OptionControlSpec<Identifier> option, boolean isDetached) {
        return OptionComponents.createTextBox(model, option, ConfigTextBox::configureForIdentifier, isDetached);
    }

    @SuppressWarnings("DataFlowIssue")
    public static Result<FlowLayout, ConfigTextBox> createColorComponent(UIModel model, OptionControlSpec<Color> option, boolean withAlpha, boolean isDetached) {
        final var result = OptionComponents.createTextBox(model, option, color -> color.asHexString(withAlpha), configTextBox -> {
            configTextBox.inputPredicate(withAlpha ? s -> s.matches("#[a-zA-Z\\d]{0,8}") : s -> s.matches("#[a-zA-Z\\d]{0,6}"));
            configTextBox.applyPredicate(withAlpha ? s -> s.matches("#[a-zA-Z\\d]{8}") : s -> s.matches("#[a-zA-Z\\d]{6}"));
            configTextBox.valueParser(withAlpha
                    ? s -> Color.ofArgb(Integer.parseUnsignedInt(s.substring(1), 16))
                    : s -> Color.ofRgb(Integer.parseUnsignedInt(s.substring(1), 16))
            );
        }, isDetached);

        result.baseComponent().<FlowLayout>configure(controls -> {
            Supplier<Color> valueGetter = () -> result.optionProvider().isValid()
                    ? (Color) result.optionProvider().parsedValue()
                    : Color.BLACK;

            var box = Components.box(Sizing.fixed(15), Sizing.fixed(15)).color(valueGetter.get()).fill(true);
            box.margins(Insets.right(5)).cursorStyle(CursorStyle.HAND);
            controls.child(0, box);

            result.optionProvider().onChanged().subscribe(value -> box.color(valueGetter.get()));

            box.mouseDown().subscribe((mouseX, mouseY, button) -> {
                ((FlowLayout) box.root()).child(Containers.overlay(
                        model.expandTemplate(
                                FlowLayout.class,
                                "color-picker-panel",
                                Map.of("color", valueGetter.get().asHexString(withAlpha), "with-alpha", String.valueOf(withAlpha))
                        ).<FlowLayout>configure(flowLayout -> {
                            var picker = flowLayout.childById(ColorPickerComponent.class, "color-picker");
                            var previewBox = flowLayout.childById(BoxComponent.class, "current-color");

                            picker.onChanged().subscribe(previewBox::color);

                            flowLayout.childById(ButtonComponent.class, "confirm-button").onPress(confirmButton -> {
                                result.optionProvider().text(picker.selectedColor().asHexString(withAlpha));
                                flowLayout.parent().remove();
                            });

                            flowLayout.childById(ButtonComponent.class, "cancel-button").onPress(cancelButton -> {
                                flowLayout.parent().remove();
                            });
                        })
                ).zIndex(100));

                return true;
            });
        });

        return result;
    }

    public static Result<FlowLayout, ConfigTextBox> createTextBox(UIModel model, OptionControlSpec<?> option, Consumer<ConfigTextBox> processor, boolean isDetached) {
        return createTextBox(model, option, Objects::toString, processor, isDetached);
    }

    public static <T> Result<FlowLayout, ConfigTextBox> createTextBox(UIModel model, OptionControlSpec<T> option, Function<T, String> toStringFunction, Consumer<ConfigTextBox> processor, boolean isDetached) {
        var optionComponent = model.expandTemplate(FlowLayout.class, "text-box-config-option", Map.of());

        var valueBox = optionComponent.childById(ConfigTextBox.class, "value-box");
        var resetButton = optionComponent.childById(ButtonComponent.class, "reset-button");

        valueBox.text(toStringFunction.apply(option.value()));

        valueBox.horizontalSizing(Sizing.fixed(Math.round(valueBox.horizontalSizing().get().value / 1.25f))); // Difference 2

        if (isDetached) {
            resetButton.active = false;
            valueBox.setEditable(false);
        } else {
            resetButton.active = !valueBox.getText().equals(toStringFunction.apply(option.defaultValue()));
            resetButton.onPress(button -> {
                valueBox.setText(toStringFunction.apply(option.defaultValue()));
                button.active = false;
            });

            valueBox.onChanged().subscribe(s -> resetButton.active = !s.equals(toStringFunction.apply(option.defaultValue())));
        }

        processor.accept(valueBox);

        optionComponent.child(new SearchAnchorComponent(
                optionComponent,
                option.key(),
                valueBox::getText
        ));

        return new Result<>(optionComponent, valueBox);
    }

    public static Result<FlowLayout, ? extends OptionValueProvider> createNumberComponent(UIModel model, OptionControlSpec<? extends Number> option, int decimalPlaces, Double min, Double max, boolean createRange, boolean isDetached) {
        return (createRange)
                ? OptionComponents.createRangeControls(model, option, decimalPlaces, min, max, isDetached)
                : OptionComponents.createTextBox(model, option, configTextBox -> configTextBox.configureForNumber(option.clazz(), min, max), isDetached);
    }

    public static Result<FlowLayout, OptionValueProvider> createRangeControls(UIModel model, OptionControlSpec<? extends Number> option, int decimalPlaces, Double min, Double max, boolean isDetached) {
        boolean withDecimals = decimalPlaces > 0;

        // ------------
        // Slider setup
        // ------------

        var value = option.value();
        var optionComponent = model.expandTemplate(FlowLayout.class, "range-config-option", Map.of());

        var sliderInput = optionComponent.childById(ConfigSlider.class, "value-slider");
        sliderInput.min(min).max(max).decimalPlaces(decimalPlaces).snap(!withDecimals).setFromDiscreteValue(value.doubleValue());
        sliderInput.valueType(option.clazz());

        var resetButton = optionComponent.childById(ButtonComponent.class, "reset-button");

        if (isDetached) {
            resetButton.active = false;
            sliderInput.active = false;
        } else {
            resetButton.active = (withDecimals ? value.doubleValue() : Math.round(value.doubleValue())) != option.defaultValue().doubleValue();
            resetButton.onPress(button -> {
                sliderInput.setFromDiscreteValue(option.defaultValue().doubleValue());
                button.active = false;
            });

            sliderInput.onChanged().subscribe(newValue -> {
                resetButton.active = (withDecimals ? newValue : Math.round(newValue)) != option.defaultValue().doubleValue();
            });
        }

        // ------------------------------------
        // Component handles and text box setup
        // ------------------------------------

        var sliderControls = optionComponent.childById(FlowLayout.class, "slider-controls");
        var textControls = createTextBox(model, option, configTextBox -> {
            configTextBox.configureForNumber(option.clazz(), min, max);

            var predicate = configTextBox.applyPredicate();
            configTextBox.applyPredicate(predicate.and(s -> {
                final var parsed = Double.parseDouble(s);
                return parsed >= min && parsed <= max;
            }));
        }, isDetached).baseComponent().positioning(Positioning.layout());
        var textInput = textControls.childById(ConfigTextBox.class, "value-box");

        // ------------
        // Toggle setup
        // ------------

        var controlsLayout = optionComponent;
        var toggleButton = optionComponent.childById(ButtonComponent.class, "toggle-button");

        var textMode = new MutableBoolean(false);
        toggleButton.onPress(button -> {
            textMode.setValue(textMode.isFalse());

            if (textMode.isTrue()) {
                sliderControls.remove();
                textInput.text(sliderInput.decimalPlaces() == 0 ? String.valueOf((int) sliderInput.discreteValue()) : String.valueOf(sliderInput.discreteValue()));

                controlsLayout.child(textControls);
            } else {
                textControls.remove();
                sliderInput.setFromDiscreteValue(((Number) textInput.parsedValue()).doubleValue());

                controlsLayout.child(sliderControls);
            }

            button.tooltip(textMode.isTrue()
                    ? Text.translatable("text.owo.config.button.range.edit_with_slider")
                    : Text.translatable("text.owo.config.button.range.edit_as_text")
            );
        });

        optionComponent.child(new SearchAnchorComponent(
                optionComponent,
                option.key(),
                () -> textMode.isTrue() ? textInput.getText() : sliderInput.getMessage().getString()
        ));

        return new Result<>(optionComponent, new OptionValueProvider() {
            @Override
            public boolean isValid() {
                return textMode.isTrue()
                        ? textInput.isValid()
                        : sliderInput.isValid();
            }

            @Override
            public Object parsedValue() {
                return textMode.isTrue()
                        ? textInput.parsedValue()
                        : sliderInput.parsedValue();
            }
        });
    }

    public static Result<FlowLayout, ConfigToggleButton> createToggleButton(UIModel model, OptionControlSpec<Boolean> option, boolean isDetached) {
        var optionComponent = model.expandTemplate(FlowLayout.class, "boolean-toggle-config-option", Map.of());

        var toggleButton = optionComponent.childById(ConfigToggleButton.class, "toggle-button");
        var resetButton = optionComponent.childById(ButtonComponent.class, "reset-button");

        toggleButton.horizontalSizing(Sizing.fixed(Math.round(toggleButton.horizontalSizing().get().value / 1.25f))); // Difference 2
        toggleButton.margins(Insets.horizontal(1));

        toggleButton.enabled(option.value());

        if (isDetached) {
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
                () -> toggleButton.getMessage().getString()
        ));

        return new Result<>(optionComponent, toggleButton);
    }

    public static Result<FlowLayout, ConfigEnumButton> createEnumButton(UIModel model, OptionControlSpec<? extends Enum<?>> option, boolean isDetached) {
        var optionComponent = model.expandTemplate(FlowLayout.class, "enum-config-option", Map.of());

        var enumButton = optionComponent.childById(ConfigEnumButton.class, "enum-button");
        var resetButton = optionComponent.childById(ButtonComponent.class, "reset-button");

        enumButton.horizontalSizing(Sizing.fixed(Math.round(enumButton.horizontalSizing().get().value / 1.25f))); // Difference 2
        enumButton.margins(Insets.horizontal(1));

        enumButton.init(option, option.value().ordinal());

        if (isDetached) {
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
                () -> enumButton.getMessage().getString()
        ));

        return new Result<>(optionComponent, enumButton);
    }
}
