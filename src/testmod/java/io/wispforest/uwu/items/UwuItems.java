package io.wispforest.uwu.items;

import io.wispforest.uwu.Uwu;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class UwuItems {

    public static final Item TEST_STICK = register("test_stick", UwuTestStickItem::new);
    public static final Item SCREEN_SHARD = register("screen_shard", UwuScreenShardItem::new);
    public static final Item COUNTER = register("counter", UwuCounterItem::new);

    public static class OwoCompatItems {
        public static final Item OWO_COMPAT_ITEM = register(Identifier.of("owo", "owo_compat_item"), new Item.Settings().group(Uwu.FOUR_TAB_GROUP).tab(2));
    }

    public static <T extends Item> T register(String path, Function<Item.Settings, T> factory) {
        return register(path, factory, new Item.Settings());
    }

    public static Item register(String path, Item.Settings settings) {
        return register(Identifier.of("uwu", path), Item::new, settings);
    }

    public static <T extends Item> T register(String path, Function<Item.Settings, T> factory, Item.Settings settings) {
        return register(Identifier.of("uwu", path), factory, settings);
    }

    public static Item register(Identifier identifier, Item.Settings settings) {
        return register(identifier, Item::new, settings);
    }


    public static <T extends Item> T register(Identifier identifier, Function<Item.Settings, T> factory, Item.Settings settings) {
        var registryKey = RegistryKey.of(RegistryKeys.ITEM, identifier);

        settings.registryKey(registryKey);

        T t = factory.apply(settings);

        return Registry.register(Registries.ITEM, registryKey, t);
    }

    public static void init() {}
}
