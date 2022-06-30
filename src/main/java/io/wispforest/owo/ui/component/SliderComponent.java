package io.wispforest.owo.ui.component;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class SliderComponent extends SliderWidget {

    public SliderComponent() {
        super(0, 0, 0, 0, Text.empty(), 0);
    }

    public SliderComponent value(double value) {
        this.value = value;
        return this;
    }

    public double value() {
        return this.value;
    }

    @Override
    protected void updateMessage() {}

    @Override
    protected void applyValue() {}
}
