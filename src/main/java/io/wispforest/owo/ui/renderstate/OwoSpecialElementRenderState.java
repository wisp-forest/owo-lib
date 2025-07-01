package io.wispforest.owo.ui.renderstate;

import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import net.minecraft.client.render.VertexConsumerProvider;

public interface OwoSpecialElementRenderState<T extends OwoSpecialElementRenderState<T>> extends SpecialGuiElementRenderState {
    SpecialGuiElementRenderer<T> createRenderer(VertexConsumerProvider.Immediate vertexConsumers);
}
