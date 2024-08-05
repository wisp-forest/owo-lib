package io.wispforest.owo.mixin.itemgroup;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.itemgroup.gui.ItemGroupButtonWidget;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.util.CursorAdapter;
import io.wispforest.owo.util.pond.OwoCreativeInventoryScreenExtensions;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Text;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
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

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin extends EffectRenderingInventoryScreen<CreativeModeInventoryScreen.ItemPickerMenu> implements OwoCreativeInventoryScreenExtensions {

    @Shadow
    private static CreativeModeTab selectedTab;

    @Shadow
    protected abstract void init();

    @Shadow
    protected abstract boolean hasPermissions(Player player);

    @Shadow
    protected abstract boolean canScroll();

    @Unique
    private final List<ItemGroupButtonWidget> owoButtons = new ArrayList<>();

    @Unique
    private FeatureFlagSet enabledFeatures = null;

    @Unique
    private final CursorAdapter cursorAdapter = CursorAdapter.ofClientWindow();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void captureFeatures(LocalPlayer player, FeatureFlagSet enabledFeatures, boolean operatorTabEnabled, CallbackInfo ci) {
        this.enabledFeatures = enabledFeatures;
    }

    // ----------
    // Background
    // ----------

    @ModifyArg(method = "renderBg", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawTexture(Lnet/minecraft/resources/Identifier;IIIIII)V", ordinal = 0))
    private Identifier injectCustomGroupTexture(Identifier original) {
        if (!(selectedTab instanceof OwoItemGroup owoGroup) || owoGroup.getBackgroundTextureOwo() == null) return original;
        return owoGroup.getBackgroundTextureOwo();
    }

    // ----------------
    // Scrollbar slider
    // ----------------

    @ModifyArgs(method = "renderBg", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawGuiTexture(Lnet/minecraft/resources/Identifier;IIII)V"))
    private void injectCustomScrollbarTexture(Args args) {
        if (!(selectedTab instanceof OwoItemGroup owoGroup) || owoGroup.getScrollerTextures() == null) return;

        args.set(0, this.canScroll() ? owoGroup.getScrollerTextures().enabled() : owoGroup.getScrollerTextures().disabled());
    }

    // -------------
    // Group headers
    // -------------

    @ModifyArg(method = "renderTabButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawGuiTexture(Lnet/minecraft/resources/Identifier;IIII)V"))
    private Identifier injectCustomTabTexture(Identifier texture, @Local(argsOnly = true) CreativeModeTab group) {
        if(!(group instanceof OwoItemGroup contextGroup) || contextGroup.getTabTextures() == null) return texture;

        var textures = contextGroup.getTabTextures();
        return contextGroup.row() == CreativeModeTab.Row.TOP
                ? selectedTab == contextGroup ? contextGroup.column() == 0 ? textures.topSelectedFirstColumn() : textures.topSelected() : textures.topUnselected()
                : selectedTab == contextGroup ? contextGroup.column() == 0 ? textures.bottomSelectedFirstColumn() : textures.bottomSelected() : textures.bottomUnselected();
    }

    @Inject(method = "renderTabButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTab;getIconItem()Lnet/minecraft/world/item/ItemStack;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void renderOwoIcon(GuiGraphics context, CreativeModeTab group, CallbackInfo ci, boolean bl, boolean bl2, int i, int j, int k) {
        if (!(group instanceof OwoItemGroup owoGroup)) return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        owoGroup.icon().render(context, j, k, 0, 0, 0);
        RenderSystem.disableBlend();
    }

    // -------------
    // oωo tab title
    // -------------

    @ModifyArg(method = "renderLabels", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawText(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Text;IIIZ)I"))
    private Text injectTabNameAsTitle(Text original) {
        if (!(selectedTab instanceof OwoItemGroup owoGroup) || !owoGroup.hasDynamicTitle() || owoGroup.selectedTabs().size() != 1) {
            return original;
        }

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

    @Inject(at = @At("HEAD"), method = "selectTab")
    private void setSelectedTab(CreativeModeTab group, CallbackInfo ci) {
        this.owoButtons.forEach(this::removeWidget);
        this.owoButtons.clear();

        if (group instanceof OwoItemGroup owoGroup) {
            int tabRootY = this.topPos;

            final var tabStackHeight = owoGroup.getTabStackHeight();
            tabRootY -= 13 * (tabStackHeight - 4);

            if (owoGroup.shouldDisplaySingleTab() || owoGroup.tabs.size() > 1) {
                for (int tabIdx = 0; tabIdx < owoGroup.tabs.size(); tabIdx++) {
                    var tab = owoGroup.tabs.get(tabIdx);

                    int xOffset = this.leftPos - 27 - (tabIdx / tabStackHeight) * 26;
                    int yOffset = tabRootY + 10 + (tabIdx % tabStackHeight) * 30;

                    var tabButton = new ItemGroupButtonWidget(xOffset, yOffset, 32, tab, owo$createSelectAction(owoGroup, tabIdx));
                    if (owoGroup.isTabSelected(tabIdx)) tabButton.isSelected = true;

                    this.owoButtons.add(tabButton);
                    this.addRenderableWidget(tabButton);
                }
            }

            final var buttonStackHeight = owoGroup.getButtonStackHeight();
            tabRootY = this.topPos - 13 * (buttonStackHeight - 4);

            var buttons = owoGroup.getButtons();
            for (int i = 0; i < buttons.size(); i++) {
                var buttonDefinition = buttons.get(i);

                int xOffset = this.leftPos + 198 + (i / buttonStackHeight) * 26;
                int yOffset = tabRootY + 10 + (i % buttonStackHeight) * 30;

                var tabButton = new ItemGroupButtonWidget(xOffset, yOffset, 0, buttonDefinition, __ -> buttonDefinition.action().run());

                this.owoButtons.add(tabButton);
                this.addRenderableWidget(tabButton);
            }
        }
    }

    @Inject(at = @At("TAIL"), method = "render")
    private void render(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        boolean anyButtonHovered = false;

        for (var button : this.owoButtons) {
            if (button.trulyHovered()) {
                context.drawTooltip(
                        this.font,
                        button.isTab() && ((OwoItemGroup) selectedTab).canSelectMultipleTabs()
                                ? List.of(button.getMessage(), Text.translatable("text.owo.itemGroup.select_hint"))
                                : List.of(button.getMessage()),
                        mouseX,
                        mouseY
                );
                anyButtonHovered = true;
            }
        }

        this.cursorAdapter.applyStyle(anyButtonHovered ? CursorStyle.HAND : CursorStyle.NONE);
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void disposeCursorAdapter(CallbackInfo ci) {
        this.cursorAdapter.dispose();
    }

    @Override
    public int owo$getRootX() {
        return this.leftPos;
    }

    @Override
    public int owo$getRootY() {
        return this.topPos;
    }

    @Unique
    private Consumer<ItemGroupButtonWidget> owo$createSelectAction(OwoItemGroup group, int tabIdx) {
        return button -> {
            var context = new CreativeModeTab.ItemDisplayParameters(this.enabledFeatures, this.hasPermissions(this.menu.player()), this.menu.player().level().registryAccess());
            if (Screen.hasShiftDown()) {
                group.toggleTab(tabIdx, context);
            } else {
                group.selectSingleTab(tabIdx, context);
            }

            this.rebuildWidgets();
            button.isSelected = true;
        };
    }

    public CreativeInventoryScreenMixin(CreativeModeInventoryScreen.ItemPickerMenu screenHandler, Inventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }
}