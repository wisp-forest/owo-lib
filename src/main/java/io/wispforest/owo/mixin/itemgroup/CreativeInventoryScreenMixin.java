package io.wispforest.owo.mixin.itemgroup;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.itemgroup.gui.ItemGroupButtonWidget;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.util.CursorAdapter;
import io.wispforest.owo.util.pond.OwoCreativeInventoryScreenExtensions;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin extends AbstractInventoryScreen<CreativeInventoryScreen.CreativeScreenHandler> implements OwoCreativeInventoryScreenExtensions {

    @Shadow
    private static ItemGroup selectedTab;

    @Shadow
    protected abstract void init();

    @Shadow
    protected abstract boolean shouldShowOperatorTab(PlayerEntity player);

    @Unique
    private final List<ItemGroupButtonWidget> owo$buttons = new ArrayList<>();

    @Unique
    private FeatureSet owo$enabledFeatures = null;

    @Unique
    private final CursorAdapter owo$cursorAdapter = CursorAdapter.ofClientWindow();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void captureFeatures(PlayerEntity player, FeatureSet enabledFeatures, boolean operatorTabEnabled, CallbackInfo ci) {
        this.owo$enabledFeatures = enabledFeatures;
    }

    // ----------
    // Background
    // ----------

    @ModifyArg(method = "drawBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V", ordinal = 0))
    private Identifier injectCustomGroupTexture(Identifier original) {
        if (!(selectedTab instanceof OwoItemGroup owoGroup) || owoGroup.getCustomTexture() == null) return original;
        return owoGroup.getCustomTexture();
    }

    // ----------------
    // Scrollbar slider
    // ----------------

    @ModifyArgs(method = "drawBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"))
    private void injectCustomScrollbarTexture(Args args) {
        if (!(selectedTab instanceof OwoItemGroup owoGroup) || owoGroup.getCustomTexture() == null) return;

        // TODO: make work.
        args.set(0, owoGroup.getCustomTexture());
    }

    // -------------
    // Group headers
    // -------------

    @ModifyArg(method = "renderTabIcon", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"))
    private Identifier injectCustomTabTexture(Identifier texture) {
        if (!(selectedTab instanceof OwoItemGroup owoGroup) || owoGroup.getCustomTexture() == null) return texture;
        return owoGroup.getCustomTexture();
    }

    @ModifyArg(method = "renderTabIcon", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"), index = 3)
    private int injectCustomTabTextureLocation(int original) {
        if (!(selectedTab instanceof OwoItemGroup owoGroup) || owoGroup.getCustomTexture() == null) return original;
        return owoGroup.getColumn() == 0 ? 195 : 221;
    }

    @Inject(method = "renderTabIcon", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemGroup;getIcon()Lnet/minecraft/item/ItemStack;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void renderOwoIcon(DrawContext context, ItemGroup group, CallbackInfo ci, boolean bl, boolean bl2, int i, int j, int k) {
        if (!(group instanceof OwoItemGroup owoGroup)) return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        owoGroup.icon().render(context, j, k, 0, 0, 0);
        RenderSystem.disableBlend();
    }

    // -------------
    // oωo tab title
    // -------------

    @ModifyArg(method = "drawForeground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)I"))
    private Text injectTabNameAsTitle(Text original) {
        if (!(selectedTab instanceof OwoItemGroup owoGroup) || !owoGroup.hasDynamicTitle() || owoGroup.selectedTabs().size() != 1) return original;

        var singleActiveTab = owoGroup.getTab(owoGroup.selectedTabs().iterator().nextInt());
        if (singleActiveTab.primary()) {
            return singleActiveTab.name();
        } else {
            return Text.translatable(
                    "text.owo.itemGroup.tab_template",
                    owoGroup.getDisplayName(),
                    singleActiveTab.name()
            );
        }
    }

    // ---------------
    // oωo tab buttons
    // ---------------

    @Inject(at = @At("HEAD"), method = "setSelectedTab(Lnet/minecraft/item/ItemGroup;)V")
    private void setSelectedTab(ItemGroup group, CallbackInfo ci) {
        this.owo$buttons.forEach(this::remove);
        this.owo$buttons.clear();

        if (group instanceof OwoItemGroup owoGroup) {
            int tabRootY = this.y;

            final var tabStackHeight = owoGroup.getTabStackHeight();
            tabRootY -= 13 * (tabStackHeight - 4);

            if (owoGroup.shouldDisplaySingleTab() || owoGroup.tabs.size() > 1) {
                for (int tabIdx = 0; tabIdx < owoGroup.tabs.size(); tabIdx++) {
                    var tab = owoGroup.tabs.get(tabIdx);

                    int xOffset = this.x - 27 - (tabIdx / tabStackHeight) * 26;
                    int yOffset = tabRootY + 10 + (tabIdx % tabStackHeight) * 30;

                    var tabButton = new ItemGroupButtonWidget(xOffset, yOffset, 32, tab, owo$createSelectAction(owoGroup, tabIdx));
                    if (owoGroup.isTabSelected(tabIdx)) tabButton.isSelected = true;

                    this.owo$buttons.add(tabButton);
                    this.addDrawableChild(tabButton);
                }
            }

            final var buttonStackHeight = owoGroup.getButtonStackHeight();
            tabRootY = this.y - 13 * (buttonStackHeight - 4);

            var buttons = owoGroup.getButtons();
            for (int i = 0; i < buttons.size(); i++) {
                var buttonDefinition = buttons.get(i);

                int xOffset = this.x + 198 + (i / buttonStackHeight) * 26;
                int yOffset = tabRootY + 10 + (i % buttonStackHeight) * 30;

                var tabButton = new ItemGroupButtonWidget(xOffset, yOffset, 0, buttonDefinition, __ -> buttonDefinition.action().run());

                this.owo$buttons.add(tabButton);
                this.addDrawableChild(tabButton);
            }
        }
    }

    @Inject(at = @At("TAIL"), method = "render")
    private void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        boolean anyButtonHovered = false;

        for (var button : this.owo$buttons) {
            if (button.trulyHovered()) {
                context.drawTooltip(
                        this.textRenderer,
                        button.isTab() && ((OwoItemGroup)selectedTab).canSelectMultipleTabs()
                                ? List.of(button.getMessage(), Text.translatable("text.owo.itemGroup.select_hint"))
                                : List.of(button.getMessage()),
                        mouseX,
                        mouseY
                );
                anyButtonHovered = true;
            }
        }

        this.owo$cursorAdapter.applyStyle(anyButtonHovered ? CursorStyle.HAND : CursorStyle.NONE);
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void disposeCursorAdapter(CallbackInfo ci) {
        this.owo$cursorAdapter.dispose();
    }

    @Override
    public int owo$getRootX() {
        return this.x;
    }

    @Override
    public int owo$getRootY() {
        return this.y;
    }

    @Unique
    private Consumer<ItemGroupButtonWidget> owo$createSelectAction(OwoItemGroup group, int tabIdx) {
        return button -> {
            var context = new ItemGroup.DisplayContext(this.owo$enabledFeatures, this.shouldShowOperatorTab(this.handler.player()), this.handler.player().getWorld().getRegistryManager());
            if (Screen.hasShiftDown()) {
                group.toggleTab(tabIdx, context);
            } else {
                group.selectSingleTab(tabIdx, context);
            }

            this.clearAndInit();
            button.isSelected = true;
        };
    }

    public CreativeInventoryScreenMixin(CreativeInventoryScreen.CreativeScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }
}