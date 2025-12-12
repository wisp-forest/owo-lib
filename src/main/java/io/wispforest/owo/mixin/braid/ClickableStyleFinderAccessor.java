package io.wispforest.owo.mixin.braid;

import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Consumer;

@Mixin(ActiveTextCollector.ClickableStyleFinder.class)
public interface ClickableStyleFinderAccessor {
    @Mutable
    @Accessor("styleScanner")
    void owo$setStyleScanner(Consumer<Style> setStyleCallback);

    @Accessor("result")
    void owo$setResult(Style style);
}
