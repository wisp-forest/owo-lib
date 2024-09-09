package io.wispforest.owo.mixin.registry;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RegistryEntry.Reference.class)
public interface ReferenceAccessor<T> {
    @Invoker("setRegistryKey")
    void owo$setRegistryKey(RegistryKey<T> registryKey);

    @Invoker("setValue")
    void owo$setValue(T value);
}
