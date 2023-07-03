package io.wispforest.owo.registration.reflect;

import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface EntityRegistryContainer extends AutoRegistryContainer<EntityType<?>> {

    @Override
    default Registry<EntityType<?>> getRegistry() {
        return Registries.ENTITY_TYPE;
    }

    @Override
    default Class<EntityType<?>> getTargetFieldType() {
        return AutoRegistryContainer.conform(EntityType.class);
    }
}
