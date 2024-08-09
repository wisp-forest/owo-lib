package io.wispforest.owo.registration.reflect.entry;

import net.minecraft.registry.entry.RegistryEntry;

public interface TypedRegistryEntry<T extends B, B> extends RegistryEntry<B> {

    @Override
    T value();
}
