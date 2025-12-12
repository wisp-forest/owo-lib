package io.wispforest.owo.mixin.ui.access;

import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Checkbox.class)
public interface CheckboxAccessor {
    @Accessor("selected")
    void owo$setSelected(boolean checked);

    @Accessor("textWidget")
    MultiLineTextWidget owo$getTextWidget();
}
