package io.wispforest.owo.mixin.ui;

import io.wispforest.owo.ui.renderstate.CubeMapElementRenderState;
import net.minecraft.client.renderer.CubeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.OptionalInt;

@Mixin(CubeMap.class)
public class CubeMapMixin {

    @ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/CommandEncoder;createRenderPass(Ljava/util/function/Supplier;Lcom/mojang/blaze3d/textures/GpuTextureView;Ljava/util/OptionalInt;Lcom/mojang/blaze3d/textures/GpuTextureView;Ljava/util/OptionalDouble;)Lcom/mojang/blaze3d/systems/RenderPass;"))
    private void injectOutputTextures(Args args) {
        if (CubeMapElementRenderState.outputOverride == null) return;

        args.set(1, CubeMapElementRenderState.outputOverride.color());
        args.set(2, OptionalInt.of(CubeMapElementRenderState.outputOverride.resetColor()));
        args.set(3, CubeMapElementRenderState.outputOverride.depth());
    }

}
