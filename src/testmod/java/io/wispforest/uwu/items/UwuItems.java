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
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.lang.reflect.Field;
import java.util.function.Function;
import java.util.function.Supplier;

public class UwuItems extends ItemRegistryContainer {

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

    //--

    public static final ItemRegistryEntry<UwuTestStickItem> TEST_STICK = item(UwuTestStickItem::new);
    public static final ItemRegistryEntry<Item> SCREEN_SHARD = item(UwuScreenShardItem::new);
    public static final ItemRegistryEntry<Item> COUNTER = item(UwuCounterItem::new);

    public static final UwuTestStickItem TEST_STICK_V2 = register("test_stick", UwuTestStickItem::new);
    public static final UwuScreenShardItem SCREEN_SHARD_V2 = register("screen_shard", UwuScreenShardItem::new);
    public static final UwuCounterItem COUNTER_V2 = register("counter", UwuCounterItem::new);

    public static <T extends Item> T register(String path, Function<Item.Settings, T> factory) {
        return register(path, factory, new Item.Settings());
    }

    public static <T extends Item> T register(String path, Function<Item.Settings, T> factory, Item.Settings settings) {
        return register(Identifier.of("uwu", path), factory, settings);
    }

    public static <T extends Item> T register(Identifier identifier, Function<Item.Settings, T> factory, Item.Settings settings) {
        var registryKey = RegistryKey.of(RegistryKeys.ITEM, identifier);

        settings.registryKey(registryKey);

        T t = factory.apply(settings);

        return Registry.register(Registries.ITEM, registryKey, t);
    }
}
