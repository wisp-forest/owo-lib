package io.wispforest.uwu.items;

import io.wispforest.owo.Owo;
import io.wispforest.owo.neoforge.api.RegistryUtils;
import io.wispforest.owo.samples.braid.BraidSamplesItem;
import io.wispforest.uwu.Uwu;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;

import java.util.function.Function;
import java.util.function.Supplier;

public class UwuItems {

    public static final Supplier<Item> TEST_STICK = register("test_stick", UwuTestStickItem::new);
    public static final Supplier<Item> SCREEN_SHARD = register("screen_shard", UwuScreenShardItem::new);
    public static final Supplier<Item> COUNTER = register("counter", UwuCounterItem::new);
    public static final Supplier<Item> BRAID = register("braid", UwuBraidItem::new);
    public static final Supplier<Item> BRAID_SAMPLES = register("braid_samples", BraidSamplesItem::new);

    public static final Supplier<Item> OWO_INGOT = register(Identifier.fromNamespaceAndPath("uowou", "owo_ingot"), new Item.Properties().group(Uwu.FOUR_TAB_GROUP).tab(2).stacksTo(69));


    public static <T extends Item> Supplier<T> register(String path, Function<Item.Properties, T> factory) {
        return register(path, factory, new Item.Properties());
    }

    public static Supplier<Item> register(String path, Item.Properties settings) {
        return register(Identifier.fromNamespaceAndPath("uwu", path), Item::new, settings);
    }

    public static <T extends Item> Supplier<T> register(String path, Function<Item.Properties, T> factory, Item.Properties settings) {
        return register(Identifier.fromNamespaceAndPath("uwu", path), factory, settings);
    }

    public static Supplier<Item> register(Identifier identifier, Item.Properties settings) {
        return register(identifier, Item::new, settings);
    }


    public static <T extends Item> Supplier<T> register(Identifier identifier, Function<Item.Properties, T> factory, Item.Properties settings) {
        var registryKey = ResourceKey.create(Registries.ITEM, identifier);

        settings.setId(registryKey);

        return RegistryUtils.registerDeferred(BuiltInRegistries.ITEM, registryKey.identifier(), () -> factory.apply(settings));
    }

    public static void init() {}
}
