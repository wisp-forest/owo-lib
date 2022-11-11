package io.wispforest.owo.mixin.itemgroup;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.item.ItemGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference"})
@Mixin(value = CreativeInventoryScreen.class, priority = 1100)
public abstract class MixinCreativeInventoryScreenMixin {

    @Shadow
    private static ItemGroup selectedTab;

    @Shadow
    private static int fabric_currentPage;

    private static final Int2ObjectMap<ItemGroup> selectedTabForPage = new Int2ObjectOpenHashMap<>();

    @Shadow(remap = false)
    private boolean fabric_isGroupVisible(ItemGroup itemGroup) {
        throw new RuntimeException();
    }

    @Shadow protected abstract void setSelectedTab(ItemGroup group);

    @Inject(method = "setSelectedTab", at = @At("TAIL"))
    private void captureSetTab(ItemGroup group, CallbackInfo ci) {
        selectedTabForPage.put(fabric_currentPage, group);
    }

    @Inject(method = "fabric_updateSelection", at = @At("HEAD"), cancellable = true, remap = false)
    private void yesThisMakesPerfectSenseAndIsVeryUsable(CallbackInfo ci) {
        if (selectedTabForPage.get(fabric_currentPage) != null) {
            this.setSelectedTab(selectedTabForPage.get(fabric_currentPage));
            ci.cancel();
            return;
        }

        if (this.fabric_isGroupVisible(selectedTab)) {
            ci.cancel();
        }
    }

}
