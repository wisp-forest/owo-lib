package io.wispforest.uwu.items;

import io.wispforest.owo.registration.annotations.AssignedName;
import io.wispforest.owo.registration.annotations.RegistryNamespace;
import io.wispforest.owo.registration.reflect.ItemRegistryContainer;
import io.wispforest.uwu.Uwu;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.Item;
import java.lang.reflect.Field;

public class UwuItems implements ItemRegistryContainer {

    public static final Item TEST_STICK = new UwuTestStickItem();
    public static final Item SCREEN_SHARD = new UwuScreenShardItem();
    public static final Item COUNTER = new UwuCounterItem();

    @RegistryNamespace("uowou")
    public static class OwoCompatItems implements ItemRegistryContainer {

        @AssignedName("owo_ingot")
        public static final Item OWO_COMPAT_ITEM = new Item(new Item.Properties().group(Uwu.FOUR_TAB_GROUP).tab(2));

        @Override
        public boolean shouldProcessField(Item value, String identifier, Field field) {
            return FabricLoader.getInstance().isModLoaded("owo");
        }
    }

}
