package io.wispforest.owo.ui.renderstate;


import io.wispforest.owo.braid.core.element.BraidBlockElement;
import io.wispforest.owo.braid.core.element.BraidEntityElement;
import io.wispforest.owo.braid.core.element.BraidItemElement;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;

public class OwoSpecialGuiElementRenderers {
    public static void init(IEventBus bus) {
        bus.addListener(RegisterPictureInPictureRenderersEvent.class, event -> {
            event.register(CubeMapElementRenderState.class, buffer -> new CubeMapElementRenderState.Renderer(buffer));
            event.register(EntityElementRenderState.class, buffer -> new EntityElementRenderState.Renderer(buffer));
            event.register(BlockElementRenderState.class, buffer -> new BlockElementRenderState.Renderer(buffer));
            event.register(LargeItemElementRenderState.class, buffer -> new OwoItemElementRenderState.Renderer(buffer));

            event.register(BraidEntityElement.class, buffer -> new BraidEntityElement.Renderer(buffer));
            event.register(BraidBlockElement.class, buffer -> new BraidBlockElement.Renderer(buffer));
            event.register(BraidItemElement.class, buffer -> new BraidItemElement.Renderer(buffer));
        });
    }
}
