package io.wispforest.owo.mixin.ui;

import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.renderstate.CubeMapElementRenderState;
import net.minecraft.client.renderer.CubeMap;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Optional;

@Mixin(CubeMap.class)
public class CubeMapMixin {

    @ModifyArgs(method = "render", require = 0, at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/commands/CommandEncoder;createRenderPass(Ljava/util/function/Supplier;Lcom/mojang/renderpearl/api/textures/GpuTextureView;Ljava/util/Optional;Lcom/mojang/renderpearl/api/textures/GpuTextureView;Ljava/util/OptionalDouble;)Lcom/mojang/renderpearl/api/commands/RenderPass;"))
    private void injectOutputTextures(Args args) {
        if (CubeMapElementRenderState.outputOverride == null) return;

        var color = Color.ofArgb(CubeMapElementRenderState.outputOverride.resetColor());

        args.set(1, CubeMapElementRenderState.outputOverride.color());
        args.set(2, Optional.of(new Vector4f(color.red(), color.green(), color.blue(), color.alpha())));
        args.set(3, CubeMapElementRenderState.outputOverride.depth());
    }

}