package io.wispforest.owo.mixin.itemgroup;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.item.ItemGroup;
import net.neoforged.neoforge.client.gui.CreativeTabsScreenPage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference"})
@Mixin(value = CreativeInventoryScreen.class, priority = 1100)
public abstract class MixinCreativeInventoryScreenMixin {

    @Shadow
    private static ItemGroup selectedTab;

//    @Shadow(remap = false) // FAPI
//    private static int currentPage;

    private static final Int2ObjectMap<ItemGroup> owo$selectedTabForPage = new Int2ObjectOpenHashMap<>();
    private static boolean owo$calledFromInit = false;

//    @Shadow(remap = false) // FAPI
//    private boolean isGroupVisible(ItemGroup itemGroup) { throw new RuntimeException(); }
//
//    @Shadow(remap = false) // FAPI
//    private void updateSelection() {}

    @Shadow(remap = false) // FORGE
    @Final
    private List<CreativeTabsScreenPage> pages;

    @Shadow(remap = false) // FORGE
    private CreativeTabsScreenPage currentPage;

    @Shadow
    protected abstract void setSelectedTab(ItemGroup group);

//    @Inject(method = "setSelectedTab", at = @At("TAIL"))
//    private void captureSetTab(ItemGroup group, CallbackInfo ci) {
//        owo$selectedTabForPage.put(currentPageIndex(), group);
//    }

//    @Inject(method = "updateSelection", at = @At("HEAD"), cancellable = true, remap = false)
//    private void yesThisMakesPerfectSenseAndIsVeryUsable(CallbackInfo ci) {
//        if (owo$selectedTabForPage.get(currentPageIndex()) != null) {
//            this.setSelectedTab(owo$selectedTabForPage.get(currentPageIndex()));
//            ci.cancel();
//            return;
//        }
//
//        if (this.currentPage.getVisibleTabs().contains(selectedTab)) {
//            ci.cancel();
//        }
//    }

//    @Inject(method = "init", at = @At("HEAD"))
//    private void prepareTheFixForTheFix(CallbackInfo ci) {
//        owo$calledFromInit = true;
//    }
//
//    @Inject(method = "getCurrentPage", at = @At("HEAD"), cancellable = true)
//    private void iLoveFixingTheFix(CallbackInfoReturnable<CreativeTabsScreenPage> cir) {
//        if (!owo$calledFromInit) return;
//
//        cir.setReturnValue(currentPage);
//        owo$calledFromInit = false;
//    }
//
//    @Inject(method = "init", at = @At("TAIL"))
//    private void endTheFixForTheFix(CallbackInfo ci) {
//        //this.updateSelection();
//        owo$calledFromInit = false;
//    }

    @Unique
    private int currentPageIndex() {
        return this.pages.indexOf(this.currentPage);
    }
}
