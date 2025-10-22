package io.wispforest.owo.ui.renderstate;

import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry;

public class OwoSpecialGuiElementRenderers {
    public static void init() {
        SpecialGuiElementRegistry.register(ctx -> new CubeMapElementRenderState.Renderer(ctx.vertexConsumers()));
        SpecialGuiElementRegistry.register(ctx -> new EntityElementRenderState.Renderer(ctx.vertexConsumers()));
        SpecialGuiElementRegistry.register(ctx -> new BlockElementRenderState.Renderer(ctx.vertexConsumers()));
        SpecialGuiElementRegistry.register(ctx -> new LargeItemElementRenderState.Renderer(ctx.vertexConsumers()));
    }
}
