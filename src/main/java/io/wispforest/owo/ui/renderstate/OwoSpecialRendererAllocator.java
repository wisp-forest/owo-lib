package io.wispforest.owo.ui.renderstate;

import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.render.VertexConsumerProvider;

import java.util.ArrayList;
import java.util.List;

public class OwoSpecialRendererAllocator {
    private final List<SpecialGuiElementRenderer<?>> renderers = new ArrayList<>();
    private int nextRendererIndex = 0;

    public SpecialGuiElementRenderer<?> alloc(OwoSpecialElementRenderState<?> state, VertexConsumerProvider.Immediate vertexConsumers) {
        if (this.nextRendererIndex >= this.renderers.size()) {
            this.renderers.add(state.createRenderer(vertexConsumers));
        }

        return this.renderers.get(this.nextRendererIndex++);
    }

    public void reset() {
        if (this.nextRendererIndex < this.renderers.size()) {
            for (int idx = this.renderers.size() - 1; idx >= this.nextRendererIndex; idx--) {
                this.renderers.remove(idx).close();
            }
        }

        this.nextRendererIndex = 0;
    }

    public void close() {
        this.renderers.forEach(SpecialGuiElementRenderer::close);
        this.renderers.clear();

        this.nextRendererIndex = 0;
    }
}
