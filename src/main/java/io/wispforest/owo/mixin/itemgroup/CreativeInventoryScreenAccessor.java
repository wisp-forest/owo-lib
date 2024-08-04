package io.wispforest.owo.mixin.itemgroup;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CreativeModeInventoryScreen.class)
public interface CreativeInventoryScreenAccessor {

    @Accessor("selectedTab")
    static CreativeModeTab owo$getSelectedTab() {
        throw new IllegalStateException("Mixin stub must not be called");
    }

}
