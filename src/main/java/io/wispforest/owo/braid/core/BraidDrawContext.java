package io.wispforest.owo.braid.core;

import io.wispforest.owo.mixin.ui.access.DrawContextAccessor;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import org.joml.Vector2d;

public class BraidDrawContext extends OwoUIDrawContext {

    private final Surface surface;

    protected BraidDrawContext(MinecraftClient client, VertexConsumerProvider.Immediate vertexConsumers, Surface surface) {
        super(client, vertexConsumers);
        this.surface = surface;
    }

    public static BraidDrawContext create(DrawContext context, Surface surface) {
        var braidContext = new BraidDrawContext(MinecraftClient.getInstance(), context.getVertexConsumers(), surface);
        ((DrawContextAccessor) braidContext).owo$setScissorStack(((DrawContextAccessor) context).owo$getScissorStack());
        ((DrawContextAccessor) braidContext).owo$setMatrices(((DrawContextAccessor) context).owo$getMatrices());

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

    public void buildRectOutline(double x, double y, double width, double height, RectEdgeBuilder builder) {
        builder.edge(x, y, x + width, y);
        builder.edge(x, y + height, x + width, y + height);

        builder.edge(x, y, x, y + height);
        builder.edge(x + width, y, x + width, y + height);
    }

    public void drawDashedLine(RenderLayer layer, double x1, double y1, double x2, double y2, double thiccness, double segmentLength, Color color) {
        var buffer = this.getVertexConsumers().getBuffer(layer);
        var matrix = this.getMatrices().peek().getPositionMatrix();
        var colorArgb = color.argb();

        var begin = new Vector2d(x1, y1);
        var end = new Vector2d(x2, y2);

        var step = end.sub(begin, new Vector2d()).normalize().mul(segmentLength);
        var segmentCount = (int) ((end.distance(begin) + segmentLength) / (segmentLength * 2));

        var offset = end.sub(begin, new Vector2d()).perpendicular().normalize().mul(thiccness * .5d);
        end.set(begin).add(step);

        step.mul(2);

        for (var i = 0; i < segmentCount; i++) {
            buffer.vertex(matrix, (float) (begin.x + offset.x), (float) (begin.y + offset.y), 0).color(colorArgb);
            buffer.vertex(matrix, (float) (begin.x - offset.x), (float) (begin.y - offset.y), 0).color(colorArgb);
            buffer.vertex(matrix, (float) (end.x - offset.x), (float) (end.y - offset.y), 0).color(colorArgb);
            buffer.vertex(matrix, (float) (end.x + offset.x), (float) (end.y + offset.y), 0).color(colorArgb);

            begin.add(step);
            end.add(step);
        }
    }

    @FunctionalInterface
    public interface RectEdgeBuilder {
        void edge(double x1, double y1, double x2, double y2);
    }
}
