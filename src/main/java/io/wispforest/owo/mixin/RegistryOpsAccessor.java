package io.wispforest.owo.mixin;

import net.minecraft.registry.RegistryOps;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RegistryOps.class)
public interface RegistryOpsAccessor {
    @Invoker("caching")
    static RegistryOps.RegistryInfoGetter owo$caching(RegistryOps.RegistryInfoGetter registryInfoGetter) {throw new UnsupportedOperationException();}

    @Accessor("registryInfoGetter")
    RegistryOps.RegistryInfoGetter owo$infoGetter();
}
