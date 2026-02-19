package io.wispforest.owo.mixin.itemgroup;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.itemgroup.v1.FabricCreativeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference"})
@Mixin(value = CreativeModeInventoryScreen.class, priority = 1100)
public abstract class MixinCreativeModeInventoryScreenMixin implements FabricCreativeInventoryScreen {

    @Unique private static final Int2ObjectMap<CreativeModeTab> selectedTabForPage = new Int2ObjectOpenHashMap<>();

    @Shadow
    protected abstract void selectTab(CreativeModeTab group);

    @Inject(method = "selectTab", at = @At("TAIL"))
    private void captureSetTab(CreativeModeTab group, CallbackInfo ci) {
        selectedTabForPage.put(getCurrentPage(), group);
    }

    @Inject(method = "updateSelection", at = @At("HEAD"), cancellable = true, remap = false)
    private void yesThisMakesPerfectSenseAndIsVeryUsable(CallbackInfo ci) {
        var selectedTab = selectedTabForPage.get(getCurrentPage());
        if (selectedTab == null) return;
        this.selectTab(selectedTab);
        ci.cancel();
    }

    //---
    // Code fixes some cases of an issue where current page value is somehow returned differently.
    // Attempted to be resolve by using MinecraftMixin to prevent off thread screen set calls

    /*
    @Unique private static boolean calledFromInit = false;

    @Shadow(remap = false) // FAPI
    private static int currentPage;
    @Shadow(remap = false) // FAPI
    private void updateSelection() {}

    @Inject(method = "init", at = @At("HEAD"))
    private void prepareTheFixForTheFix(CallbackInfo ci) {
        calledFromInit = true;
    }

    @Inject(method = "getCurrentPage", at = @At("HEAD"), cancellable = true)
    private void iLoveFixingTheFix(CallbackInfoReturnable<Integer> cir) {
        if (!calledFromInit) return;

        cir.setReturnValue(currentPage);
        calledFromInit = false;
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void endTheFixForTheFix(CallbackInfo ci) {
        this.updateSelection();
        calledFromInit = false;
    }
    */
}
