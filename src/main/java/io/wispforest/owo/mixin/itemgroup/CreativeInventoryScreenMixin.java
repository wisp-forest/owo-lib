package io.wispforest.owo.mixin.itemgroup;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.wispforest.owo.itemgroup.base.OwoItemGroup;
import io.wispforest.owo.itemgroup.base.OwoItemGroupState;
import io.wispforest.owo.itemgroup.impl.CondensedEntryStates;
import io.wispforest.owo.itemgroup.gui.IconRenderRegistry;
import io.wispforest.owo.itemgroup.gui.ItemGroupButtonWidget;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.util.CursorAdapter;
import io.wispforest.owo.util.pond.OwoCreativeInventoryScreenExtensions;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin extends HandledScreen<CreativeInventoryScreen.CreativeScreenHandler> implements OwoCreativeInventoryScreenExtensions {

    @Shadow
    private static ItemGroup selectedTab;

    @Shadow
    protected abstract void init();

    @Shadow
    protected abstract boolean shouldShowOperatorTab(PlayerEntity player);

    @Shadow
    protected abstract boolean hasScrollbar();

    @Shadow protected abstract void refreshSelectedTab(Collection<ItemStack> displayStacks);

    @Unique
    private final List<ItemGroupButtonWidget> owoButtons = new ArrayList<>();

    @Unique
    private FeatureSet enabledFeatures = null;

    @Unique
    private final CursorAdapter cursorAdapter = CursorAdapter.ofClientWindow();

    @Nullable
    private static OwoItemGroupState currentState = null;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void captureFeatures(ClientPlayerEntity player, FeatureSet enabledFeatures, boolean operatorTabEnabled, CallbackInfo ci) {
        this.enabledFeatures = enabledFeatures;
    }

    // ----------------
    // Background texture
    // ----------------

    @ModifyArg(method = "drawBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIFFIIII)V", ordinal = 0))
    private Identifier injectCustomGroupTexture(Identifier original) {
        var extension = OwoItemGroup.get(selectedTab);

        return (extension != null && extension.backgroundTexture() != null)
            ? extension.backgroundTexture()
            : original;
    }

    // ----------------
    // Scrollbar slider
    // ----------------

    @ModifyArg(method = "drawBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIII)V"))
    private Identifier injectCustomScrollbarTexture(Identifier texture) {
        var extension = OwoItemGroup.get(selectedTab);

        return (extension != null && extension.scrollerTextures() != null)
            ? extension.scrollerTextures().getTexture(this.hasScrollbar())
            : texture;
    }

    // -------------
    // Group headers
    // -------------

    @ModifyArg(method = "renderTabIcon", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIII)V"))
    private Identifier injectCustomTabTexture(Identifier texture, @Local(argsOnly = true) ItemGroup group) {
        var extension = OwoItemGroup.get(group);

        return (extension != null && extension.tabTextures() != null)
            ? extension.tabTextures().getTexture(group, selectedTab)
            : texture;
    }

    @Unique private float delta = 0;
    @Unique private int mouseX = 0;
    @Unique private int mouseY = 0;

    @Inject(method = "render", at = @At("HEAD"))
    private void setRenderContextInfo(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        this.delta = 0;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    @Inject(method = "renderTabIcon", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemGroup;getIcon()Lnet/minecraft/item/ItemStack;"))
    private void renderOwoIcon(DrawContext context, ItemGroup group, CallbackInfo ci, @Local(ordinal = 3) int j, @Local(ordinal = 4) int k) {
        var extension = OwoItemGroup.get(group);

        if (extension != null) {
            IconRenderRegistry.renderIcon(extension.icon(), context, j, k, mouseX, mouseY, delta);
        }
    }

    // -------------
    // oωo tab title
    // -------------

    @ModifyArg(method = "drawForeground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)V"))
    private Text injectTabNameAsTitle(Text original) {
        return (currentState != null)
            ? currentState.getDisplayName(original)
            : original;
    }

    // ---------------
    // oωo tab buttons
    // ---------------

    @Inject(at = @At("HEAD"), method = "setSelectedTab(Lnet/minecraft/item/ItemGroup;)V")
    private void setSelectedTab(ItemGroup group, CallbackInfo ci) {
        this.owoButtons.forEach(this::remove);
        this.owoButtons.clear();

        currentState = OwoItemGroupState.get(group);

        if (currentState != null) {
            var extension = currentState.getExtension();

            int tabRootY = this.y;

            final var tabStackHeight = extension.tabStackHeight();
            tabRootY -= 13 * (tabStackHeight - 4);

            if (extension.getTabs().size() > 1) {
                var tabs = extension.getTabs();

                for (int tabIdx = 0; tabIdx < tabs.size(); tabIdx++) {
                    var tab = tabs.get(tabIdx);

                    int xOffset = this.x - 27 - (tabIdx / tabStackHeight) * 26;
                    int yOffset = tabRootY + 10 + (tabIdx % tabStackHeight) * 30;

                    var tabButton = new ItemGroupButtonWidget(xOffset, yOffset, 32, tab, owo$createSelectAction(currentState, tabIdx));
                    if (currentState.isTabSelected(tabIdx)) tabButton.isSelected = true;

                    this.owoButtons.add(tabButton);
                    this.addDrawableChild(tabButton);
                }
            }

            final var buttonStackHeight = extension.buttonStackHeight();
            tabRootY = this.y - 13 * (buttonStackHeight - 4);

            var buttons = extension.getButtons();
            for (int i = 0; i < buttons.size(); i++) {
                var buttonDefinition = buttons.get(i);

                int xOffset = this.x + 198 + (i / buttonStackHeight) * 26;
                int yOffset = tabRootY + 10 + (i % buttonStackHeight) * 30;

                var tabButton = new ItemGroupButtonWidget(xOffset, yOffset, 0, buttonDefinition, __ -> buttonDefinition.action().run());

                this.owoButtons.add(tabButton);
                this.addDrawableChild(tabButton);
            }
        }
    }

    @Inject(at = @At("TAIL"), method = "render")
    private void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        boolean anyButtonHovered = false;

        if (currentState != null) {
            for (var button : this.owoButtons) {
                if (button.trulyHovered()) {
                    context.drawTooltip(
                        this.textRenderer,
                        button.isTab() && currentState.getExtension().allowMultiSelect()
                            ? List.of(button.getMessage(), Text.translatable("text.owo.itemGroup.select_hint"))
                            : List.of(button.getMessage()),
                        mouseX,
                        mouseY,
                        null
                    );
                    anyButtonHovered = true;
                }
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
        return this.x;
    }

    @Override
    public int owo$getRootY() {
        return this.y;
    }

    @Unique
    private Consumer<ItemGroupButtonWidget> owo$createSelectAction(OwoItemGroupState state, int tabIdx) {
        return button -> {
            var context = new ItemGroup.DisplayContext(this.enabledFeatures, this.shouldShowOperatorTab(this.handler.player()), this.handler.player().getWorld().getRegistryManager());
            if (Screen.hasShiftDown()) {
                state.toggleTab(tabIdx, context);
            } else {
                state.selectSingleTab(tabIdx, context);
            }

            this.clearAndInit();
            button.isSelected = true;
        };
    }

    public CreativeInventoryScreenMixin(CreativeInventoryScreen.CreativeScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }

    //--

    @WrapOperation(method = "setSelectedTab", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/DefaultedList;addAll(Ljava/util/Collection;)Z", ordinal = 1))
    private boolean adjustStacksIfCondensedEntries(DefaultedList instance, Collection<ItemStack> collection, Operation<Boolean> original, @Local(argsOnly = true) ItemGroup group) {
        var list = new ArrayList<>(collection);

        var tabIndex = (currentState != null) ? currentState.selectedTabs() : IntSet.of(0);

        CondensedEntryStates.handleCondensedEntries(group, tabIndex, list);

        return original.call(instance, list);
    }

    @Inject(method = "onMouseClick", at = @At(value = "HEAD"), cancellable = true)
    private void attemptEntryHandling(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        if (!(slot instanceof CreativeInventoryScreen.LockableSlot)) return;

        var state = CondensedEntryStates.getState(slot.getStack());

        if (state == null || !state.isParent()) return;

        var list = new ArrayList<>(handler.itemList);

        state.state().toggleChildren(list);

        this.refreshSelectedTab(list);

        ci.cancel();
    }
}