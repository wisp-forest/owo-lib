package io.wispforest.owo.mixin.ui.access;

import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CheckboxWidget.class)
public interface CheckboxWidgetAccessor {
    @Accessor("checked")
    void owo$setChecked(boolean checked);

    @Accessor("textWidget")
    MultilineTextWidget owo$getTextWidget();
}
