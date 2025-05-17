package io.wispforest.owo.braid.widgets.cycle;

import net.minecraft.text.Text;

@FunctionalInterface
public interface CyclerMessageProvider<T> {
    Text getMessage(T value);
}
