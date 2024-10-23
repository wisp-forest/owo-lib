package io.wispforest.owo.registration.reflect;

/**
 * Due to changes within 1.21.2 makes {@link AutoRegistryContainer} impossible due to requiring
 * the {@link net.minecraft.registry.RegistryKey} leading to the need to supplier the key before
 * fully creating the desired entry.
 * </br></br>
 * Recommend using Minecraft methods for registering such entry
 */
@Deprecated(forRemoval = true)
public interface EntityRegistryContainer /*extends AutoRegistryContainer<EntityType<?>>*/ {
//    @Override
//    default Registry<EntityType<?>> getRegistry() {
//        return Registries.ENTITY_TYPE;
//    }
//
//    @Override
//    default Class<EntityType<?>> getTargetFieldType() {
//        return AutoRegistryContainer.conform(EntityType.class);
//    }
}
