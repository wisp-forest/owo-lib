package io.wispforest.owo.mixin.registry;

import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(SimpleRegistry.class)
public interface SimpleRegistryAccessor<T> {
    @Accessor("valueToEntry")
    Map<T, RegistryEntry.Reference<T>> owo$getValueToEntry();

    @Accessor("idToEntry")
    Map<Identifier, RegistryEntry.Reference<T>> owo$getIdToEntry();
}
