package io.wispforest.owo.registration.reflect;

import net.minecraft.entity.EntityType;
import net.minecraft.util.registry.Registries;
import net.minecraft.util.registry.Registry;

public interface EntityRegistryContainer extends AutoRegistryContainer<EntityType<?>> {

    @Override
    default Registry<EntityType<?>> getRegistry() {
        return Registries.ENTITY_TYPE;
    }

    @Override
    @SuppressWarnings("unchecked")
    default Class<EntityType<?>> getTargetFieldType() {
        return (Class<EntityType<?>>) (Object) EntityType.class;
    }
}
