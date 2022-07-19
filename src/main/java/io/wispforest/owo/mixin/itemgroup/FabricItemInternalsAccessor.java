package io.wispforest.owo.mixin.itemgroup;

import net.fabricmc.fabric.api.item.v1.CustomDamageHandler;
import net.fabricmc.fabric.api.item.v1.EquipmentSlotProvider;
import net.fabricmc.fabric.impl.item.FabricItemInternals;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.WeakHashMap;

@Mixin(value = FabricItemInternals.class, remap = false)
public interface FabricItemInternalsAccessor {

    @Accessor("extraData")
    static WeakHashMap<Item.Settings, FabricItemInternals.ExtraData> owo$getExtraData() {
        throw new UnsupportedOperationException();
    }

    @Mixin(value = FabricItemInternals.ExtraData.class, remap = false)
    interface ExtraDataAccessor {
        @Accessor("equipmentSlotProvider")
        EquipmentSlotProvider owo$getEquipmentSlotProvider();

        @Accessor("customDamageHandler")
        CustomDamageHandler owo$getCustomDamageHandler();
    }
}


