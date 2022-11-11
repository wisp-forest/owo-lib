package io.wispforest.owo.mixin.itemgroup;

import io.wispforest.owo.itemgroup.json.OwoItemGroupLoader;
import net.fabricmc.fabric.impl.itemgroup.ItemGroupHelper;
import net.minecraft.item.ItemGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnstableApiUsage")
@Mixin(value = ItemGroupHelper.class, remap = false)
public class ItemGroupHelperMixin {

    @Inject(method = "appendItemGroup", at = @At("TAIL"))
    private static void injectWrappers(ItemGroup itemGroup, CallbackInfo ci) {
        OwoItemGroupLoader.onGroupCreated(itemGroup);
    }

}
