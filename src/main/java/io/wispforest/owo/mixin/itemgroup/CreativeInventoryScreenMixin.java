package io.wispforest.owo.mixin.itemgroup;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import io.wispforest.owo.Owo;
import io.wispforest.owo.itemgroup.base.OwoItemGroupState;
import io.wispforest.owo.itemgroup.util.DisplayContextUtils;
import io.wispforest.owo.itemgroup.gui.OwoItemGroupRendererHandler;
import io.wispforest.owo.itemgroup.impl.CondensedEntryStates;
import io.wispforest.owo.mixin.itemgroup.fabric.ItemGroupButtonWidgetAccessor;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.fabricmc.fabric.impl.client.itemgroup.FabricCreativeGuiComponents;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.apache.commons.lang3.mutable.MutableObject;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin extends HandledScreen<CreativeInventoryScreen.CreativeScreenHandler> {

    @Shadow
    protected abstract boolean hasScrollbar();

    @Shadow protected abstract void refreshSelectedTab(Collection<ItemStack> displayStacks);

    public CreativeInventoryScreenMixin(CreativeInventoryScreen.CreativeScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }

    // Setup owo itemgroup tab buttons and additional side buttons
    @Inject(at = @At("HEAD"), method = "setSelectedTab(Lnet/minecraft/item/ItemGroup;)V")
    private void setSelectedTab(ItemGroup group, CallbackInfo ci) {
        OwoItemGroupRendererHandler.setupRenderer(group, x, y, ((CreativeInventoryScreen) (Object) this));
    }

    // Setup extra render info for things like the Icon renderers
    @Inject(method = "render", at = @At("HEAD"))
    private void setRenderContextInfo(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        OwoItemGroupRendererHandler.getSelectedRenderer().beforeRender(mouseX, mouseY, delta);
    }

    // Background texture
    @WrapOperation(method = "drawBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIFFIIII)V", ordinal = 0))
    private void injectCustomGroupTexture(DrawContext context, RenderPipeline pipeline, Identifier sprite, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, Operation<Void> original) {
        var spriteHolder = new MutableObject<>(sprite);

        if (OwoItemGroupRendererHandler.getSelectedRenderer().renderBackground(context, x, y, spriteHolder::setValue)) return;

        original.call(context, pipeline, spriteHolder.getValue(), x, y, u, v, width, height, textureWidth, textureHeight);
    }

    // Scrollbar slider
    @WrapOperation(method = "drawBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIII)V"))
    private void injectCustomScrollbarTexture(DrawContext context, RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height, Operation<Void> original) {
        var spriteHolder = new MutableObject<>(sprite);

        if (OwoItemGroupRendererHandler.getSelectedRenderer().renderScrollbar(context, x, y, this.hasScrollbar(), spriteHolder::setValue)) return;

        original.call(context, pipeline, spriteHolder.getValue(), x, y, width, height);
    }

    // Group headers
    @WrapOperation(method = "renderTabIcon", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIII)V"))
    private void injectCustomTabTexture(DrawContext context, RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height, Operation<Void> original, @Local(argsOnly = true) ItemGroup group) {
        var spriteHolder = new MutableObject<>(sprite);

        if (OwoItemGroupRendererHandler.getRenderer(group).renderTab(context, x, y, group, spriteHolder::setValue)) return;

        original.call(context, pipeline, spriteHolder.getValue(), x, y, width, height);
    }

    // Icon Rendering
    @WrapOperation(method = "renderTabIcon", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawItem(Lnet/minecraft/item/ItemStack;II)V"))
    private void renderOwoIcon(DrawContext context, ItemStack item, int x, int y, Operation<Void> original, @Local(argsOnly = true) ItemGroup group, @Local(ordinal = 3) int j, @Local(ordinal = 4) int k) {
        var instance = OwoItemGroupRendererHandler.getRenderer(group);

        if (instance.renderIcon(context, j, k)) return;

        original.call(context, item, x, y);
    }

    // oωo tab title
    @WrapOperation(method = "drawForeground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)V"))
    private void injectTabNameAsTitle(DrawContext context, TextRenderer textRenderer, Text text, int x, int y, int color, boolean shadow, Operation<Void> original) {
        var textHolder = new MutableObject<>(text);

        var renderer = OwoItemGroupRendererHandler.getSelectedRenderer();

        if (renderer.renderTitle(context, x, y, text, textHolder::setValue)) return;

        if (renderer != OwoItemGroupRendererHandler.EMPTY) {
            int width = (expandEntriesButton != null ? expandEntriesButton.getX() : this.x + 171 - 15) - this.x - 3 - x;

            var owoCtx = OwoUIDrawContext.of(context);

            owoCtx.drawScrollableText(textHolder.getValue(), x, y, width, textRenderer.fontHeight, 0, color, false, shadow);
        } else {
            original.call(context, textRenderer, textHolder.getValue(), x, y, color, shadow);
        }
    }

    // Handle drawing the tooltip for buttons message and multi select hint
    @Inject(at = @At("TAIL"), method = "render")
    private void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        OwoItemGroupRendererHandler.getSelectedRenderer().afterRender();
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void disposeOfInstance(CallbackInfo ci) {
        OwoItemGroupRendererHandler.disposeInstances();
    }

    //--

    @Unique
    private static final Identifier EXPAND_BUTTON_TEXTURE = Identifier.of("owo", "textures/gui/condensed_entry/expand_button.png");

    @Unique
    private boolean anyEntryExpanded = false;

    @Unique
    private ButtonWidget expandEntriesButton = null;

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/CreativeInventoryScreen;setSelectedTab(Lnet/minecraft/item/ItemGroup;)V"))
    private void setupCondensedEntryToggle(CallbackInfo ci) {
        ButtonWidget buttonRoot = null;

        for (var owo$drawable : ((ScreenAccessor) this).owo$drawables()) {
            if (owo$drawable instanceof FabricCreativeGuiComponents.ItemGroupButtonWidget itemGroupButtonWidget) {
                var type = ((ItemGroupButtonWidgetAccessor) itemGroupButtonWidget).owo$type();

                if (type == FabricCreativeGuiComponents.Type.PREVIOUS) {
                    buttonRoot = itemGroupButtonWidget;

                    break;
                }
            }
        }


        int x, y;

        if (buttonRoot != null) {
            x = buttonRoot.getX();
            y = buttonRoot.getY();
        } else {
            x = this.x + 171;
            y = this.y + 4;
        }

        x -= 15;

        if (Owo.CONFIG.showExpandEntriesButton()) {
            expandEntriesButton = this.addDrawableChild(new ButtonWidget(x, y, 12, 12, Text.empty(), button -> {
                updateStackEntries(itemStacks -> {
                    CondensedEntryStates.forAllState(state -> {
                        if (anyEntryExpanded) {
                            if (state.showChildren()) {
                                state.toggleChildren(itemStacks);
                            }
                        } else {
                            state.toggleChildren(itemStacks);
                        }
                    });

                    anyEntryExpanded = !anyEntryExpanded;
                });

            }, Supplier::get) {
                @Override
                protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
                    //this.active = type.isEnabled.test(screen);
                    //this.visible = screen.hasAdditionalPages();

                    if (!this.visible) return;

                    int u = active && this.isHovered() ? 12 : 0;
                    int v = active ? 0 : 12;

                    context.drawTexture(RenderPipelines.GUI_TEXTURED, EXPAND_BUTTON_TEXTURE, this.getX(), this.getY(), u, v, 12, 12, 24, 24);

                    if (this.isHovered() && this.active) {
                        var type = anyEntryExpanded ? "collapse" : "expand";

                        context.drawTooltip(textRenderer, Text.translatable("text.owo.condensed_entries." + type), mouseX, mouseY);
                    }
                }
            });
        }
    }

    // Handle general setup of condensed entries for the given selected group and the toggled tabs
    @WrapOperation(method = "setSelectedTab", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/DefaultedList;addAll(Ljava/util/Collection;)Z", ordinal = 1))
    private boolean adjustStacksIfCondensedEntries(DefaultedList instance, Collection<ItemStack> collection, Operation<Boolean> original, @Local(argsOnly = true) ItemGroup group) {
        var state = OwoItemGroupState.get(group, true);

        var list = new ArrayList<>(collection);

        var tabIndex = (state != null) ? state.selectedTabs() : IntSet.of(0);

        var wasEntriesAdded = CondensedEntryStates.handleCondensedEntries(DisplayContextUtils.createClientContext(), group, tabIndex, list);

        if (expandEntriesButton != null) {
            expandEntriesButton.active = wasEntriesAdded;
        }

        anyEntryExpanded = false;

        return original.call(instance, list);
    }


    // Attempt to check if a condensed entry should be expanded
    @Inject(method = "onMouseClick", at = @At(value = "HEAD"), cancellable = true)
    private void attemptEntryHandling(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        if (!(slot instanceof CreativeInventoryScreen.LockableSlot)) return;

        var results = CondensedEntryStates.getState(slot.getStack());

        if (results == null || !results.isParent()) return;

        updateStackEntries(itemStacks -> {
            results.state().toggleChildren(itemStacks);

            anyEntryExpanded = true;
        });

        ci.cancel();
    }

    @Unique
    private void updateStackEntries(Consumer<ArrayList<ItemStack>> consumer) {
        var list = new ArrayList<>(handler.itemList);

        consumer.accept(list);

        this.refreshSelectedTab(list);
    }
}