package io.wispforest.owo.mixin.itemgroup;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(CreativeModeTab.class)
public class CreativeModeTabMixin {
    // Added to ensure that we don't at least break binary compatibility with Yarn-mapped mods.
    @SuppressWarnings("DataFlowIssue")
    @Unique
    public Identifier getBackgroundTexture() {
        return ((CreativeModeTab)(Object) this).getBackgroundTexture();
    }
}
