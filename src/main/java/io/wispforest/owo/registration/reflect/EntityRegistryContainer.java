package io.wispforest.owo.registration.reflect;

import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public abstract class EntityRegistryContainer extends AutoRegistryContainer<EntityType<?>> {

    @Override
    public final Registry<EntityType<?>> getRegistry() {
        return Registries.ENTITY_TYPE;
    }

    @Override
    public final Class<EntityType<?>> getTargetFieldType() {
        return AutoRegistryContainer.conform(EntityType.class);
    }
}
