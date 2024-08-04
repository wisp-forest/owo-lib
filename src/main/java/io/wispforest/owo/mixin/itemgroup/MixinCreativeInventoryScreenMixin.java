package io.wispforest.owo.mixin.itemgroup;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference"})
@Mixin(value = CreativeModeInventoryScreen.class, priority = 1100)
public abstract class MixinCreativeInventoryScreenMixin {

    @Shadow
    private static CreativeModeTab selectedTab;

    @Shadow(remap = false) // FAPI
    private static int currentPage;

    private static final Int2ObjectMap<CreativeModeTab> owo$selectedTabForPage = new Int2ObjectOpenHashMap<>();
    private static boolean owo$calledFromInit = false;

    @Shadow(remap = false) // FAPI
    private boolean isGroupVisible(CreativeModeTab itemGroup) { throw new RuntimeException(); }

    @Shadow(remap = false) // FAPI
    private void updateSelection() {}

    @Shadow
    protected abstract void setSelectedTab(CreativeModeTab group);

    @Inject(method = "setSelectedTab", at = @At("TAIL"))
    private void captureSetTab(CreativeModeTab group, CallbackInfo ci) {
        owo$selectedTabForPage.put(currentPage, group);
    }

    @Inject(method = "updateSelection", at = @At("HEAD"), cancellable = true, remap = false)
    private void yesThisMakesPerfectSenseAndIsVeryUsable(CallbackInfo ci) {
        if (owo$selectedTabForPage.get(currentPage) != null) {
            this.setSelectedTab(owo$selectedTabForPage.get(currentPage));
            ci.cancel();
            return;
        }

        if (this.isGroupVisible(selectedTab)) {
            ci.cancel();
        }
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void prepareTheFixForTheFix(CallbackInfo ci) {
        owo$calledFromInit = true;
    }

    @Inject(method = "getCurrentPage", at = @At("HEAD"), cancellable = true)
    private void iLoveFixingTheFix(CallbackInfoReturnable<Integer> cir) {
        if (!owo$calledFromInit) return;

        cir.setReturnValue(currentPage);
        owo$calledFromInit = false;
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void endTheFixForTheFix(CallbackInfo ci) {
        this.updateSelection();
        owo$calledFromInit = false;
    }
}
