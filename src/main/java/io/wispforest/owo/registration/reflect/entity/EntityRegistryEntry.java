package io.wispforest.owo.registration.reflect.entity;

import io.wispforest.owo.registration.reflect.entry.MemoizedRegistryEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKey;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

public class EntityRegistryEntry<E extends Entity> extends MemoizedRegistryEntry<EntityType<E>, EntityType<?>> {

    private final MutableObject<RegistryKey<EntityType<?>>> registryKey;

    public EntityRegistryEntry(EntityType.Builder<E> builder) {
        this(builder, new MutableObject<>());
    }

    private EntityRegistryEntry(EntityType.Builder<E> builder, MutableObject<RegistryKey<EntityType<?>>> registryKey) {
        super(() -> builder.build(registryKey.getValue()));

        this.registryKey = registryKey;
    }

    @Override
    public void setup(RegistryKey<EntityType<?>> registryKey) {
        this.registryKey.setValue(registryKey);
    }
}
