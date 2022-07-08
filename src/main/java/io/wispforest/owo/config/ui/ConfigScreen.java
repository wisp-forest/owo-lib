package io.wispforest.owo.config.ui;

import io.wispforest.owo.config.ConfigWrapper;
import io.wispforest.owo.config.annotation.RangeConstraint;
import io.wispforest.owo.ui.BaseUIModelScreen;
import io.wispforest.owo.ui.definitions.Component;
import io.wispforest.owo.ui.layout.FlowLayout;
import io.wispforest.owo.ui.layout.VerticalFlowLayout;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.util.NumberReflection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ConfigScreen extends BaseUIModelScreen<FlowLayout> {

    private final ConfigWrapper<?> config;
    private final List<OptionAndComponent> options;

    public ConfigScreen(ConfigWrapper<?> config) {
        super(FlowLayout.class, DataSource.file("config_ui.xml"));
        this.config = config;
        this.options = new ArrayList<>();
    }

    @Override
    @SuppressWarnings({"ConstantConditions", "unchecked"})
    protected void build(FlowLayout rootComponent) {
        this.options.clear();
        var panel = rootComponent.childById(VerticalFlowLayout.class, "config-panel");

        this.config.forEachOption(option -> {
            var field = option.fieldReference().field();

            if (field.getType() == CharSequence.class) {
                panel.child(this.createTextBox(option, configTextBox -> {
                    if (option.constraint() != null) {
                        configTextBox.applyPredicate(option.constraint()::test);
                    }
                }));
            } else if (NumberReflection.isNumberType(field.getType()) && field.getType() != char.class) {
                boolean floatingPoint = NumberReflection.isFloatingPointType(field.getType());

                if (field.isAnnotationPresent(RangeConstraint.class)) {
                    panel.child(this.createSlider(option, floatingPoint));
                } else {
                    panel.child(this.createTextBox(option, configTextBox -> {
                        configTextBox.configureForNumber((Class<? extends Number>) field.getType());
                    }));
                }
            }
        });
    }

    @Override
    public void removed() {
        for (var optionAndComponent : this.options) {
            if (!optionAndComponent.component.isValid()) continue;
            optionAndComponent.option.update(optionAndComponent.component.parsedValue());
        }
        super.removed();
    }

    @SuppressWarnings({"ConstantConditions"})
    protected Component createTextBox(ConfigWrapper.ConfigOption option, Consumer<ConfigTextBox> processor) {
        var optionComponent = this.model.expandTemplate(FlowLayout.class,
                "text-box-config-option",
                packParameters("text.config." + option.configName() + ".option" + option.key(), option.cast().toString())
        );

        var valueBox = optionComponent.childById(ConfigTextBox.class, "value-box");
        var resetButton = optionComponent.childById(ButtonWidget.class, "reset-button");
        resetButton.active = !valueBox.getText().equals(option.castDefault().toString());
        resetButton.onPress(button -> {
            valueBox.setText(option.castDefault().toString());
            button.active = false;
        });

        valueBox.setChangedListener(s -> resetButton.active = !s.equals(option.castDefault().toString()));
        processor.accept(valueBox);

        this.options.add(new OptionAndComponent(option, valueBox));
        return optionComponent;
    }

    @SuppressWarnings("unchecked")
    protected Component createSlider(ConfigWrapper.ConfigOption option, boolean withDecimals) {
        var value = option.<Number>cast();
        var optionComponent = this.model.expandTemplate(FlowLayout.class,
                "range-config-option",
                packParameters("text.config." + option.configName() + ".option" + option.key(), value.toString())
        );

        var constraint = option.fieldReference().field().getAnnotation(RangeConstraint.class);
        double min = constraint.min(), max = constraint.max();

        var valueSlider = optionComponent.childById(ConfigSlider.class, "value-slider");
        valueSlider.min(min).max(max).decimalPlaces(withDecimals ? 2 : 0).setFromValue(value.doubleValue());

        var resetButton = optionComponent.childById(ButtonWidget.class, "reset-button");
        resetButton.active = (withDecimals ? value.doubleValue() : Math.round(value.doubleValue())) != option.<Number>castDefault().doubleValue();
        resetButton.onPress(button -> {
            valueSlider.setFromValue(option.<Number>castDefault().doubleValue());
            button.active = false;
        });

        valueSlider.valueType((Class<? extends Number>) option.fieldReference().field().getType());
        valueSlider.onChanged(newValue -> {
            resetButton.active = (withDecimals ? newValue : Math.round(newValue)) != option.<Number>castDefault().doubleValue();
        });

        this.options.add(new OptionAndComponent(option, valueSlider));
        return optionComponent;
    }

    protected static Map<String, String> packParameters(String name, String value) {
        return Map.of(
                "config-option-name", name,
                "config-option-value", value
        );
    }

    private record OptionAndComponent(ConfigWrapper.ConfigOption option, ConfigOptionComponent component) {}

    static {
        UIParsing.registerFactory("config-slider", element -> new ConfigSlider());
        UIParsing.registerFactory("config-text-box", element -> {
            return new ConfigTextBox(MinecraftClient.getInstance().textRenderer, 0, 0, 0, 0, Text.empty());
        });
    }
}
