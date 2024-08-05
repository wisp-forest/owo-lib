package io.wispforest.owo.mixin.ui.access;

import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.MultilineTextField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MultiLineEditBox.class)
public interface EditBoxWidgetAccessor {

    @Accessor("textField")
    MultilineTextField owo$getEditBox();

}
