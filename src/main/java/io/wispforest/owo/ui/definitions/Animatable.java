package io.wispforest.owo.ui.definitions;

public interface Animatable<T extends Animatable<T>> {

    T interpolate(T next, float delta);

}
