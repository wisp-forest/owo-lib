package io.wispforest.owo.mixin.ui;

import com.mojang.serialization.Lifecycle;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(SimpleRegistry.class)
public interface SimpleRegistryAccessor<T> {

    @Accessor("valueToEntry")
    Map<T, RegistryEntry.Reference<T>> owo$getValueToEntry();

}
