package io.wispforest.owo.braid.core;

import io.wispforest.owo.mixin.ui.DrawContextInvoker;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;

public class BraidDrawContext extends OwoUIDrawContext {

    private final Surface surface;

    protected BraidDrawContext(MinecraftClient client, VertexConsumerProvider.Immediate vertexConsumers, Surface surface) {
        super(client, vertexConsumers);
        this.surface = surface;
    }

    public static BraidDrawContext create(DrawContext context, Surface surface) {
        var braidContext = new BraidDrawContext(MinecraftClient.getInstance(), ((DrawContextInvoker)context).owo$vertexConsumers(), surface);
        ((DrawContextInvoker) braidContext).owo$setScissorStack(((DrawContextInvoker) context).owo$getScissorStack());
        ((DrawContextInvoker) braidContext).owo$setMatrices(((DrawContextInvoker) context).owo$getMatrices());

        return braidContext;
    }

    @Override
    public int getScaledWindowWidth() {
        return this.surface.width();
    }

    @Override
    public int getScaledWindowHeight() {
        return this.surface.height();
    }
}
