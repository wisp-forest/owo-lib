package io.wispforest.uwu.client;

import io.wispforest.owo.ui.BaseOwoScreen;
import io.wispforest.owo.ui.OwoUIAdapter;
import io.wispforest.owo.ui.definitions.Component;
import io.wispforest.owo.ui.definitions.ParentComponent;
import io.wispforest.owo.ui.layout.VerticalFlowLayout;
import io.wispforest.owo.ui.parse.OwoUISpec;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;

import java.nio.file.Path;
import java.util.Map;

public class TestParseScreen extends BaseOwoScreen<ParentComponent> {

    private final OwoUISpec spec;

    public TestParseScreen() {
        this.spec = OwoUISpec.load(Path.of("config_ui.xml"));
    }

    @Override
    protected OwoUIAdapter<ParentComponent> createAdapter() {
        return this.spec.createAdapter(this);
    }

    @Override
    protected void build(ParentComponent rootComponent) {
        var panel = rootComponent.<VerticalFlowLayout>childById("config-panel");

        for (int i = 0; i < 25; i++) {
            panel.child(i % 2 == 0
                    ? this.createTextOption(i)
                    : this.createRangeOption(i)
            );
        }
    }

    protected Component createTextOption(final int index) {
        var option = this.spec.expandTemplate(
                "text-config-option",
                Map.of(
                        "config-option-name", "very epic option # " + index,
                        "config-option-value", String.valueOf(index * index)
                )
        );

        var valueBox = ((ParentComponent) option).<TextFieldWidget>childById("value-box");
        ((ParentComponent) option).<ButtonWidget>childById("reset-button").onPress(button -> {
            valueBox.setText(String.valueOf(index * index));
        });

        return option;
    }

    protected Component createRangeOption(final int index) {
        var option = this.spec.expandTemplate(
                "range-config-option",
                Map.of(
                        "config-option-name", "very epic option # " + index,
                        "config-option-value", String.valueOf(index * index)
                )
        );

        var valueSlider = ((ParentComponent) option).<SliderWidget>childById("value-slider");
        valueSlider.onClick(valueSlider.x + ((index * index) / 600d) * valueSlider.width(), 0);

        ((ParentComponent) option).<ButtonWidget>childById("reset-button").onPress(button -> {
            valueSlider.onClick(valueSlider.x + ((index * index) / 600d) * valueSlider.width(), 0);
        });

        return option;
    }
}