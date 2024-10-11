package io.wispforest.owo.registration.reflect.entity;

import io.wispforest.owo.registration.reflect.entry.MemoizedRegistryEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

import java.util.function.Supplier;

public class EntityRegistryEntry<E extends Entity> extends MemoizedRegistryEntry<EntityType<E>, EntityType<?>> {
    public EntityRegistryEntry(Supplier<EntityType<E>> factory) {
        super(factory);
    }
}
