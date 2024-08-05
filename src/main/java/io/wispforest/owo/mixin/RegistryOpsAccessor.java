package io.wispforest.owo.mixin;

import net.minecraft.resources.RegistryOps;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RegistryOps.class)
public interface RegistryOpsAccessor {
    @Accessor("lookupProvider")
    RegistryOps.RegistryInfoLookup owo$infoGetter();
}
