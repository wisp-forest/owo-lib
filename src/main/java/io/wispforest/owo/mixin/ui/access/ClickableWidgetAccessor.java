package io.wispforest.owo.mixin.ui.access;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.WidgetTooltipHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractWidget.class)
public interface ClickableWidgetAccessor {

    @Accessor("height")
    void owo$setHeight(int height);

    @Accessor("width")
    void owo$setWidth(int width);

    @Accessor("x")
    void owo$setX(int x);

    @Accessor("y")
    void owo$setY(int y);

    @Accessor("tooltip")
    WidgetTooltipHolder owo$getTooltip();
}
