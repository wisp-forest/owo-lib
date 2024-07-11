package io.wispforest.uwu.items;

import io.wispforest.owo.itemgroup.OwoItemSettings;
import io.wispforest.owo.registration.annotations.AssignedName;
import io.wispforest.owo.registration.annotations.RegistryNamespace;
import io.wispforest.owo.registration.reflect.ItemRegistryContainer;
import io.wispforest.owo.registration.reflect.MemorizedEntry;
import io.wispforest.uwu.Uwu;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.component.type.FoodComponents;
import net.minecraft.item.Item;
import net.minecraft.registry.entry.RegistryEntry;

import java.lang.reflect.Field;
import java.util.function.Supplier;

public class UwuItems implements ItemRegistryContainer {

    public static final Item TEST_STICK = new UwuTestStickItem();
    public static final Item SCREEN_SHARD = new UwuScreenShardItem();

    @RegistryNamespace("uowou")
    public static class OwoCompatItems implements ItemRegistryContainer {

        @AssignedName("owo_ingot")
        public static final Item OWO_COMPAT_ITEM = new Item(new OwoItemSettings().group(Uwu.FOUR_TAB_GROUP).tab(2));

        @Override
        public boolean shouldProcessField(Item value, String identifier, Field field) {
            return FabricLoader.getInstance().isModLoaded("owo");
        }
    }

    @RegistryNamespace("supplied")
    public static class OwoTestingSuppliers implements ItemRegistryContainer {
        public static final Supplier<Item> RANDOM_1 = MemorizedEntry.of(() -> new Item(new Item.Settings().fireproof().maxCount(1)));
        public static final Supplier<Item> RANDOM_2 = MemorizedEntry.of(() -> new Item(new Item.Settings().food(FoodComponents.APPLE).maxCount(65)));
        public static final RegistryEntry<Item> RANDOM_3 = MemorizedEntry.ofEntry(() -> new Item(new Item.Settings().food(FoodComponents.APPLE).maxCount(65)));
        public static final Supplier<Item> RANDOM_4 = MemorizedEntry.ofEntry(() -> new Item(new Item.Settings().food(FoodComponents.APPLE).maxCount(65)));
    }
}
