package io.wispforest.owo.util.pond;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryInfo;
import org.jetbrains.annotations.ApiStatus;

public interface OwoSimpleRegistryExtensions<T> {

    @ApiStatus.Internal
    RegistryEntry.Reference<T> owo$set(int id, RegistryKey<T> arg, T object, RegistryEntryInfo arg2);
}
