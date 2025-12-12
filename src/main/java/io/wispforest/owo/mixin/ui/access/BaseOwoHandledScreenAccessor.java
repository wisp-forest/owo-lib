package io.wispforest.owo.mixin.ui.access;

import io.wispforest.owo.ui.base.BaseOwoContainerScreen;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = BaseOwoContainerScreen.class, remap = false)
public interface BaseOwoHandledScreenAccessor {
    @Accessor("uiAdapter")
    OwoUIAdapter<?> owo$getUIAdapter();
}
