package io.wispforest.owo.braid.widgets.slider;

@FunctionalInterface
public interface SliderCallback {
    void accept(double newValue);
}
