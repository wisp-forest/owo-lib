package com.glisco.uwu.items;

import com.glisco.owo.itemgroup.OwoItemSettings;
import com.glisco.owo.registration.annotations.AssignedName;
import com.glisco.owo.registration.annotations.RegistryNamespace;
import com.glisco.owo.registration.reflect.ItemRegistryContainer;
import com.glisco.uwu.Uwu;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;

public class UwuItems implements ItemRegistryContainer {

    public static final Item TEST_STICK = new UwuTestStickItem();

    @RegistryNamespace("uowou")
    public static class OwoCompatItems implements ItemRegistryContainer {

        @AssignedName("owo_ingot")
        public static final Item OWO_COMPAT_ITEM = new Item(new OwoItemSettings().group(Uwu.FOUR_TAB_GROUP).tab(2));

        @Override
        public boolean shouldProcessField(Item value, String identifier) {
            return FabricLoader.getInstance().isModLoaded("owo");
        }
    }

}
