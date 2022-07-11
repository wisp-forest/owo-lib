package io.wispforest.owo.config.ui;

import io.wispforest.owo.ui.component.SliderComponent;
import io.wispforest.owo.util.NumberReflection;
import net.minecraft.text.Text;

public class ConfigSlider extends SliderComponent implements OptionComponent {

    protected Class<? extends Number> valueType;
    protected double min = 0, max = 1;
    protected int decimalPlaces = 2;

    public void setFromValue(double value) {
        this.value((value - min) / (max - min));
    }

    @Override
    protected void applyValue() {
        this.listeners.set(this.min + this.value * (this.max - this.min));
    }

    @Override
    protected void updateMessage() {
        this.setMessage(Text.literal(String.format("%." + decimalPlaces + "f", this.min + this.value * (this.max - this.min))));
    }

    public ConfigSlider min(double min) {
        this.min = min;
        return this;
    }

    public double min() {
        return this.min;
    }

    public ConfigSlider max(double max) {
        this.max = max;
        return this;
    }

    public double max() {
        return this.max;
    }

    public ConfigSlider decimalPlaces(int decimalPlaces) {
        this.decimalPlaces = decimalPlaces;
        return this;
    }

    public double decimalPlaces() {
        return this.decimalPlaces;
    }

    public ConfigSlider valueType(Class<? extends Number> valueType) {
        this.valueType = valueType;
        return this;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Object parsedValue() {
        double value = this.min + this.value * (this.max - this.min);
        if (!NumberReflection.isFloatingPointType(this.valueType)) {
            value = Math.round(value);
        }

        return NumberReflection.convert(value, this.valueType);
    }
}
