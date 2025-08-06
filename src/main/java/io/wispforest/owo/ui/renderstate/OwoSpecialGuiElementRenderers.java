package io.wispforest.owo.ui.renderstate;


import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;

public class OwoSpecialGuiElementRenderers {
    public static void init(IEventBus bus) {
        bus.addListener(RegisterPictureInPictureRenderersEvent.class, event -> {
            event.register(CubeMapElementRenderState.class, buffer -> new CubeMapElementRenderState.Renderer(buffer));
            event.register(EntityElementRenderState.class, buffer -> new EntityElementRenderState.Renderer(buffer));
            event.register(BlockElementRenderState.class, buffer -> new BlockElementRenderState.Renderer(buffer));
            event.register(LargeItemElementRenderState.class, buffer -> new LargeItemElementRenderState.Renderer(buffer));
        });
    }
}
