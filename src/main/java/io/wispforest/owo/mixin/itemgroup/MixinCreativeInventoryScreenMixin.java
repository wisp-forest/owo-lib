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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference"})
@Mixin(value = CreativeInventoryScreen.class, priority = 1100)
public abstract class MixinCreativeInventoryScreenMixin {

    @Shadow
    private static ItemGroup selectedTab;

    @Shadow
    private static int fabric_currentPage;

    private static final Int2ObjectMap<ItemGroup> owo$selectedTabForPage = new Int2ObjectOpenHashMap<>();
    private static boolean owo$calledFromInit = false;

    @Shadow(remap = false)
    private boolean fabric_isGroupVisible(ItemGroup itemGroup) {
        throw new RuntimeException();
    }

    @Shadow(remap = false)
    private void fabric_updateSelection() {}

    @Shadow
    protected abstract void setSelectedTab(ItemGroup group);

    @Inject(method = "setSelectedTab", at = @At("TAIL"))
    private void captureSetTab(ItemGroup group, CallbackInfo ci) {
        owo$selectedTabForPage.put(fabric_currentPage, group);
    }

    @Inject(method = "fabric_updateSelection", at = @At("HEAD"), cancellable = true, remap = false)
    private void yesThisMakesPerfectSenseAndIsVeryUsable(CallbackInfo ci) {
        if (owo$selectedTabForPage.get(fabric_currentPage) != null) {
            this.setSelectedTab(owo$selectedTabForPage.get(fabric_currentPage));
            ci.cancel();
            return;
        }

        if (this.fabric_isGroupVisible(selectedTab)) {
            ci.cancel();
        }
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void prepareTheFixForTheFix(CallbackInfo ci) {
        owo$calledFromInit = true;
    }

    @Inject(method = "fabric_getPage", at = @At("HEAD"), cancellable = true)
    private static void iLoveFixingTheFix(CallbackInfoReturnable<Integer> cir) {
        if (!owo$calledFromInit) return;

        cir.setReturnValue(fabric_currentPage);
        owo$calledFromInit = false;
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void endTheFixForTheFix(CallbackInfo ci) {
        this.fabric_updateSelection();
        owo$calledFromInit = false;
    }

}
