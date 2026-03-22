package io.wispforest.owo.ui.renderstate;

import io.wispforest.owo.braid.core.element.BraidBlockElement;
import io.wispforest.owo.braid.core.element.BraidEntityElement;
import io.wispforest.owo.braid.core.element.BraidItemElement;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;

public class OwoSpecialGuiElementRenderers {
    public static void init() {
        PictureInPictureRendererRegistry.register(ctx -> new CubeMapElementRenderState.Renderer(ctx.bufferSource()));
        PictureInPictureRendererRegistry.register(ctx -> new EntityElementRenderState.Renderer(ctx.bufferSource()));
        PictureInPictureRendererRegistry.register(ctx -> new BlockElementRenderState.Renderer(ctx.bufferSource()));
        PictureInPictureRendererRegistry.register(ctx -> new OwoItemElementRenderState.Renderer(ctx.bufferSource()));

        PictureInPictureRendererRegistry.register(ctx -> new BraidEntityElement.Renderer(ctx.bufferSource()));
        PictureInPictureRendererRegistry.register(ctx -> new BraidBlockElement.Renderer(ctx.bufferSource()));
        PictureInPictureRendererRegistry.register(ctx -> new BraidItemElement.Renderer(ctx.bufferSource()));
    }
}
