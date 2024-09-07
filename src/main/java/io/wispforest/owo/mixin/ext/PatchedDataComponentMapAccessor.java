package io.wispforest.owo.mixin.ext;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.PatchedDataComponentMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PatchedDataComponentMap.class)
public interface PatchedDataComponentMapAccessor {
    @Accessor("prototype")
    DataComponentMap owo$getPrototype();

    @Accessor("prototype")
    @Mutable
    void owo$setPrototype(DataComponentMap prototype);
}
