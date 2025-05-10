package io.wispforest.owo.mixin;

import io.wispforest.owo.ui.core.OwoUIDrawContext;
import net.minecraft.client.gl.GlResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(GlResourceManager.class)
public class GlResourceManagerMixin {

    @ModifyArgs(
        method = "createRenderPass(Lcom/mojang/blaze3d/textures/GpuTexture;Ljava/util/OptionalInt;Lcom/mojang/blaze3d/textures/GpuTexture;Ljava/util/OptionalDouble;)Lcom/mojang/blaze3d/systems/RenderPass;",
        at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_viewport(IIII)V")
    )
    public void injectViewportOverride(Args args) {
        if (OwoUIDrawContext.viewportOverride == null) return;
        args.set(0, OwoUIDrawContext.viewportOverride.position().x());
        args.set(1, OwoUIDrawContext.viewportOverride.position().y());
        args.set(2, OwoUIDrawContext.viewportOverride.width());
        args.set(3, OwoUIDrawContext.viewportOverride.height());
    }

}
