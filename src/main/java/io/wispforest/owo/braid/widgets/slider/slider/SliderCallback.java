package io.wispforest.owo.braid.widgets.slider.slider;

@FunctionalInterface
public interface SliderCallback {
    void accept(double newValue);
}
