package io.wispforest.owo.mixin.itemgroup;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.wispforest.owo.itemgroup.impl.CondensedEntryStates;
import io.wispforest.owo.itemgroup.util.CondensedEntryRenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen {
    @Shadow protected @Nullable Slot focusedSlot;

    @Shadow
    @Final
    protected T handler;

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @Unique
    private double currentDelta = 0;

    @Nullable
    @Unique
    private CondensedEntryStates.Results getResult(Slot slot) {
        return (slot instanceof CreativeInventoryScreen.LockableSlot)
            ? getResult(slot.getStack())
            : null;
    }

    @Nullable
    @Unique
    private CondensedEntryStates.Results getResult(ItemStack stack) {
        return CondensedEntryStates.getState(stack);
    }

    @Inject(method = "drawSlots", at = @At("HEAD"))
    private void renderBackgroundElements(DrawContext context, CallbackInfo ci) {
        this.currentDelta = MinecraftClient.getInstance().getRenderTickCounter().getDynamicDeltaTicks();

        for(var slot : this.handler.slots) {
            if (!slot.isEnabled()) continue;

            var result = getResult(slot);

            if (result != null) {
                CondensedEntryRenderUtils.renderBackground(context, slot, result.state());
            }
        }
    }

    @WrapOperation(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;getStack()Lnet/minecraft/item/ItemStack;"))
    private ItemStack adjustItemStack(Slot slot, Operation<ItemStack> original) {
        var result = getResult(slot);

        return (result != null && result.isParent())
            ? result.state().getDisplayStack(currentDelta)
            : original.call(slot);
    }

    @Inject(method = "drawSlots", at = @At("TAIL"))
    private void renderForegroundElements(DrawContext context, CallbackInfo ci) {
        for(var slot : this.handler.slots) {
            if (!slot.isEnabled()) continue;

            var result = getResult(slot);

            if (result != null) {
                CondensedEntryRenderUtils.renderOnTop(context, slot, result.isParent(), result.state());
            }
        }
    }

    @WrapOperation(method = "drawMouseoverTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/util/Identifier;)V"))
    private void adjustTooltipForEntry(DrawContext instance, TextRenderer textRenderer, List<Text> text, Optional<TooltipData> data, int x, int y, @Nullable Identifier texture, Operation<Void> original, @Local(ordinal = 0) ItemStack stack) {
        if (this.focusedSlot instanceof CreativeInventoryScreen.LockableSlot) {
            var result = getResult(stack);

            if (result != null && result.isParent()) {
                text = new ArrayList<>();

                var type = client.options.advancedItemTooltips ? TooltipType.Default.ADVANCED : TooltipType.Default.BASIC;

                result.state().appendTooltip(type.withCreative(), text::add);

                data = Optional.empty();
                texture = null;
            }
        }

        original.call(instance, textRenderer, text, data, x, y, texture);
    }
}
