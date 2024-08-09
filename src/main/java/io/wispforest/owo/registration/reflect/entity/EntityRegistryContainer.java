package io.wispforest.owo.registration.reflect.entity;

import io.wispforest.owo.registration.reflect.AutoRegistryContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.function.Supplier;

public abstract class EntityRegistryContainer extends AutoRegistryContainer<EntityType<?>> {

    @Override
    public final Registry<EntityType<?>> getRegistry() {
        return Registries.ENTITY_TYPE;
    }

    @Override
    public final Class<EntityType<?>> getTargetFieldType() {
        return AutoRegistryContainer.conform(EntityType.class);
    }

    public static <E extends Entity> EntityRegistryEntry<E> entity(Supplier<EntityType<E>> supplier) {
        return new EntityRegistryEntry<>(supplier);
    }
}
