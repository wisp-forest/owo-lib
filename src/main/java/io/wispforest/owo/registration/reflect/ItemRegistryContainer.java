package io.wispforest.owo.registration.reflect;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

/**
 * Due to changes within 1.21.2 makes {@link AutoRegistryContainer} impossible due to requiring
 * the {@link net.minecraft.registry.RegistryKey} leading to the need to supplier the key before
 * fully creating the desired entry.
 * </br></br>
 * Recommend using Minecraft methods for registering such entry
 */
@Deprecated(forRemoval = true)
public interface ItemRegistryContainer /*extends AutoRegistryContainer<Item>*/ {
//    @Override
//    default Registry<Item> getRegistry() {
//        return Registries.ITEM;
//    }
//
//    @Override
//    default Class<Item> getTargetFieldType() {
//        return Item.class;
//    }
}
