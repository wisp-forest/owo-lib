package io.wispforest.owo.mixin.itemgroup.fabric;

import net.fabricmc.fabric.impl.client.itemgroup.FabricCreativeGuiComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = FabricCreativeGuiComponents.ItemGroupButtonWidget.class, remap = false)
public interface ItemGroupButtonWidgetAccessor {
    @Accessor(value = "type", remap = false)
    FabricCreativeGuiComponents.Type owo$type();
}
