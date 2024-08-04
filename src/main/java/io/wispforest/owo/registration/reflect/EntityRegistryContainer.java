package io.wispforest.owo.registration.reflect;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;

public interface EntityRegistryContainer extends AutoRegistryContainer<EntityType<?>> {

    @Override
    default Registry<EntityType<?>> getRegistry() {
        return BuiltInRegistries.ENTITY_TYPE;
    }

    @Override
    default Class<EntityType<?>> getTargetFieldType() {
        return AutoRegistryContainer.conform(EntityType.class);
    }
}
