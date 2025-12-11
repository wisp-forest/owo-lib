package io.wispforest.owo.mixin.braid;

import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Consumer;

@org.spongepowered.asm.mixin.Mixin(net.minecraft.client.font.DrawnTextConsumer.ClickHandler.class)
public interface ClickHandlerAccessor {
    @Mutable
    @Accessor("setStyleCallback")
    void owo$setSetStyleCallback(Consumer<Style> setStyleCallback);

    @Accessor("style")
    void owo$setStyle(Style style);
}
