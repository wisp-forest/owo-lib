package io.wispforest.owo.braid.widgets.slider.range;

@FunctionalInterface
public interface RangeSliderCallback {
    void accept(double newMin, double newMax);
}
