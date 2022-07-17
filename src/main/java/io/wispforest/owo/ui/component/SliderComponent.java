package io.wispforest.owo.ui.component;

import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.util.Observable;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class SliderComponent extends SliderWidget {

    protected Observable<Double> listeners;

    protected SliderComponent() {
        super(0, 0, 0, 0, Text.empty(), 0);
        this.listeners = Observable.of(this.value);
        this.verticalSizing(Sizing.fixed(20));
    }

    public SliderComponent value(double value) {
        this.value = value;
        this.updateMessage();
        this.applyValue();
        return this;
    }

    public double value() {
        return this.value;
    }

    public void onChanged(Consumer<Double> listener) {
        this.listeners.observe(listener);
    }

    @Override
    protected void updateMessage() {}

    @Override
    protected void applyValue() {
        this.listeners.set(this.value);
    }
}
