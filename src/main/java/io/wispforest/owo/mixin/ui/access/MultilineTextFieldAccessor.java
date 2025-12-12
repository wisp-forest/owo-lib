package io.wispforest.owo.mixin.ui.access;

import net.minecraft.client.gui.components.MultilineTextField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MultilineTextField.class)
public interface MultilineTextFieldAccessor {

    @Mutable
    @Accessor("width")
    void owo$setWidth(int width);

    @Accessor("selectCursor")
    void owo$setSelectCursor(int width);

    @Accessor("selectCursor")
    int owo$getSelectCursor();

}
