package io.wispforest.owo.mixin.itemgroup;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.itemgroup.v1.FabricCreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.item.ItemGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CreativeInventoryScreen.class, priority = 1100)
public abstract class MixinCreativeInventoryScreenMixin {

    @Shadow
    private static ItemGroup selectedTab;

    @Unique
    private static final Int2ObjectMap<ItemGroup> owo$selectedTabForPage = new Int2ObjectOpenHashMap<>();

    @Shadow
    protected abstract void setSelectedTab(ItemGroup group);

    @Unique
    private FabricCreativeInventoryScreen owo$self() {
        return (FabricCreativeInventoryScreen) (Object) this;
    }

    @Inject(method = "setSelectedTab", at = @At("TAIL"))
    private void captureSetTab(ItemGroup group, CallbackInfo ci) {
        owo$selectedTabForPage.put(owo$self().getCurrentPage(), group);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void restoreTabSelection(CallbackInfo ci) {
        int currentPage = owo$self().getCurrentPage();
        ItemGroup savedTab = owo$selectedTabForPage.get(currentPage);

        if (savedTab != null && owo$self().getPage(savedTab) == currentPage) {
            this.setSelectedTab(savedTab);
        }
    }
}
