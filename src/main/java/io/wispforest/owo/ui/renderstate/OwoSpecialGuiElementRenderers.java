package io.wispforest.owo.ui.renderstate;

import io.wispforest.owo.braid.core.element.BraidBlockElement;
import io.wispforest.owo.braid.core.element.BraidEntityElement;
import io.wispforest.owo.braid.core.element.BraidItemElement;
//import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;

public class OwoSpecialGuiElementRenderers {
    public static void init(RegisterPictureInPictureRenderersEvent event) {
        event.register(CubeMapElementRenderState.class, CubeMapElementRenderState.Renderer::new);
        event.register(EntityElementRenderState.class, EntityElementRenderState.Renderer::new);
        event.register(BlockElementRenderState.class, BlockElementRenderState.Renderer::new);
        event.register(OwoItemElementRenderState.class, OwoItemElementRenderState.Renderer::new);

        event.register(BraidEntityElement.class, BraidEntityElement.Renderer::new);
        event.register(BraidBlockElement.class, BraidBlockElement.Renderer::new);
        event.register(BraidItemElement.class, BraidItemElement.Renderer::new);
    }
}
