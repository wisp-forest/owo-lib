package io.wispforest.owo.mixin.itemgroup.fabric;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import io.wispforest.owo.itemgroup.gui.OwoItemGroupRendererHandler;
import net.fabricmc.fabric.impl.client.itemgroup.FabricCreativeGuiComponents;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FabricCreativeGuiComponents.ItemGroupButtonWidget.class)
public abstract class ItemGroupButtonWidgetMixin {
    @WrapOperation(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIFFIIII)V", ordinal = 0), remap = false)
    private void injectCustomGroupTexture(DrawContext context, RenderPipeline pipeline, Identifier sprite, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, Operation<Void> original) {
        var spriteHolder = new MutableObject<>(sprite);

        if (OwoItemGroupRendererHandler.getSelectedRenderer().renderPageButtons(context, x, y, spriteHolder::setValue)) return;

        original.call(context, pipeline, spriteHolder.getValue(), x, y, u, v, width, height, textureWidth, textureHeight);
    }
}
