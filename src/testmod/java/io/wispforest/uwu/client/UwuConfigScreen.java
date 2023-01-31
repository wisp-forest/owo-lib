package io.wispforest.uwu.client;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.SliderComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import net.minecraft.client.gui.widget.TextFieldWidget;

import java.util.Map;

public class UwuConfigScreen extends BaseUIModelScreen<FlowLayout> {

    public UwuConfigScreen() {
        super(FlowLayout.class, DataSource.file("config.xml"));
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        var panel = rootComponent.childById(FlowLayout.class, "config-panel");

        for (int i = 1; i <= 25; i++) {
            panel.child(i % 2 == 0
                    ? this.createTextOption(i)
                    : this.createRangeOption(i)
            );
        }
    }

    protected Component createTextOption(final int index) {
        var option = this.model.expandTemplate(FlowLayout.class,
                "text-config-option",
                Map.of(
                        "config-option-name", "very epic option #" + index,
                        "config-option-value", String.valueOf(index * index)
                )
        );

        var valueBox = option.childById(TextFieldWidget.class, "value-box");
        option.childById(ButtonComponent.class, "reset-button").onPress(button -> {
            valueBox.setText(String.valueOf(index * index));
        });

        return option;
    }

    protected Component createRangeOption(final int index) {
        var option = this.model.expandTemplate(FlowLayout.class,
                "range-config-option",
                Map.of(
                        "config-option-name", "very epic option #" + index,
                        "config-option-value", String.valueOf(index * index)
                )
        );

        var valueSlider = option.childById(SliderComponent.class, "value-slider");
        valueSlider.value((index * index) / 625d);

        option.childById(ButtonComponent.class, "reset-button").onPress(button -> {
            valueSlider.value((index * index) / 625d);
        });

        return option;
    }
}
