package io.wispforest.uwu.client;

import io.wispforest.owo.ui.BaseUISpecScreen;
import io.wispforest.owo.ui.component.SliderComponent;
import io.wispforest.owo.ui.definitions.Component;
import io.wispforest.owo.ui.layout.FlowLayout;
import io.wispforest.owo.ui.layout.VerticalFlowLayout;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;

import java.util.Map;

public class TestParseScreen extends BaseUISpecScreen<FlowLayout> {

    public TestParseScreen() {
        super(FlowLayout.class, DataSource.debug("config_ui.xml"));
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        var panel = rootComponent.childById(VerticalFlowLayout.class, "config-panel");
        long now = System.nanoTime();

        for (int i = 1; i <= 25; i++) {
            panel.child(i % 2 == 0
                    ? this.createTextOption(i)
                    : this.createRangeOption(i)
            );
        }

        long diff = System.nanoTime() - now;
        System.out.printf("Config screen built in %.3fms\n", diff / 1000000f);
    }

    protected Component createTextOption(final int index) {
        var option = this.spec.expandTemplate(FlowLayout.class,
                "text-config-option",
                Map.of(
                        "config-option-name", "very epic option #" + index,
                        "config-option-value", String.valueOf(index * index)
                )
        );

        var valueBox = option.childById(TextFieldWidget.class, "value-box");
        option.childById(ButtonWidget.class, "reset-button").onPress(button -> {
            valueBox.setText(String.valueOf(index * index));
        });

        return option;
    }

    protected Component createRangeOption(final int index) {
        var option = this.spec.expandTemplate(FlowLayout.class,
                "range-config-option",
                Map.of(
                        "config-option-name", "very epic option #" + index,
                        "config-option-value", String.valueOf(index * index)
                )
        );

        var valueSlider = option.childById(SliderComponent.class, "value-slider");
        valueSlider.value((index * index) / 625d);

        option.childById(ButtonWidget.class, "reset-button").onPress(button -> {
            valueSlider.value((index * index) / 625d);
        });

        return option;
    }
}