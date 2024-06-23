package io.wispforest.owo.mixin;

import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RegistryOps.CachedRegistryInfoGetter.class)
public interface CachedRegistryInfoGetterAccessor {
    @Accessor("registriesLookup") RegistryWrapper.WrapperLookup getRegistriesLookup();
}
