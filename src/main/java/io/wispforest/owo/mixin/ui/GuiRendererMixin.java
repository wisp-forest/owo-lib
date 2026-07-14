package io.wispforest.owo.mixin.ui;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.wispforest.owo.ui.renderstate.BlurQuadElementRenderState;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GuiRenderer.class)
public class GuiRendererMixin {

    @Shadow
    private TextureSetup previousTextureSetup;

    // FIXME 26.2: Blur rendering needs rewrite - inRenderPass hack removed from GlCommandEncoder.
    // The frame buffer copy + blur uniform binding pipeline needs a 26.2-native approach.

    @ModifyExpressionValue(method = "addElementToMesh", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/TextureSetup;equals(Ljava/lang/Object;)Z"))
    private boolean adjustCheckForBlurElements(boolean original, @Local(argsOnly = true) GuiElementRenderState state) {
        return original && !(state instanceof BlurQuadElementRenderState || BlurQuadElementRenderState.hasBlurSetupFor(previousTextureSetup));
    }
}
