package io.wispforest.uwu.items;

import io.wispforest.owo.registration.annotations.AssignedName;
import io.wispforest.owo.registration.annotations.RegistryNamespace;
import io.wispforest.owo.registration.reflect.item.ItemRegistryContainer;
import io.wispforest.owo.registration.reflect.item.ItemRegistryEntry;
import io.wispforest.owo.registration.reflect.entry.TypedRegistryEntry;
import io.wispforest.uwu.Uwu;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.component.type.FoodComponents;
import net.minecraft.item.Item;
import net.minecraft.registry.entry.RegistryEntry;

import java.lang.reflect.Field;
import java.util.function.Supplier;

public class UwuItems extends ItemRegistryContainer {

    public static final ItemRegistryEntry<UwuTestStickItem> TEST_STICK = item(UwuTestStickItem::new);
    public static final ItemRegistryEntry<Item> SCREEN_SHARD = item(UwuScreenShardItem::new);
    public static final ItemRegistryEntry<Item> COUNTER = item(UwuCounterItem::new);

    @RegistryNamespace("uowou")
    public static class OwoCompatItems extends ItemRegistryContainer {

        @AssignedName("owo_ingot")
        public static final ItemRegistryEntry<Item> OWO_COMPAT_ITEM = item(settings -> new Item(settings.group(Uwu.FOUR_TAB_GROUP).tab(2)));

        @Override
        public boolean shouldProcessField(Item value, String identifier, Field field) {
            return shouldProcessField(() -> value, identifier, field);
        }

        @Override
        public boolean shouldProcessField(Supplier<Item> value, String identifier, Field field) {
            return FabricLoader.getInstance().isModLoaded("owo");
        }
    }

    @RegistryNamespace("supplied")
    public static class OwoTestingSuppliers extends ItemRegistryContainer {
        public static final ItemRegistryEntry<Item> RANDOM_1 = item((settings) -> new Item(settings.fireproof().maxCount(1)));
        public static final ItemRegistryEntry<Item> RANDOM_2 = item((settings) -> new Item(settings.food(FoodComponents.APPLE).maxCount(65)));
        public static final ItemRegistryEntry<Item> RANDOM_3 = item((settings) -> new Item(settings.food(FoodComponents.APPLE).maxCount(65)));
        public static final ItemRegistryEntry<Item> RANDOM_4 = item((settings) -> new Item(settings.food(FoodComponents.APPLE).maxCount(65)));
    }
}
