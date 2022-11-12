package io.wispforest.owo.mixin.ui;

import net.minecraft.class_7919;
import net.minecraft.client.gui.widget.ClickableWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClickableWidget.class)
public interface ClickableWidgetAccessor {

    @Accessor("height")
    void owo$setHeight(int height);

    @Accessor("width")
    void owo$setWidth(int width);

    @Accessor("x")
    void owo$setX(int x);

    @Accessor("y")
    void owo$setY(int y);

    @Accessor("field_41095")
    class_7919 owo$getTooltip();
}
