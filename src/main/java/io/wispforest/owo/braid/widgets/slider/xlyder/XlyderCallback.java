package io.wispforest.owo.braid.widgets.slider.xlyder;

@FunctionalInterface
public interface XlyderCallback {
    void accept(double newX, double newY);
}
