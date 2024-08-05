package io.wispforest.owo.mixin.registry;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Holder.Reference.class)
public interface ReferenceAccessor<T> {
    @Invoker("bindKey")
    void owo$setRegistryKey(ResourceKey<T> registryKey);

    @Invoker("bindValue")
    void owo$setValue(T value);
}
