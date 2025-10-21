package io.wispforest.owo.braid.core.element;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import io.wispforest.owo.braid.core.Color;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import org.joml.Matrix3x2f;
import org.joml.Vector2d;

public record BraidDashedLineElement(
    Color color,
    double thiccness,
    double segmentLength,
    RenderPipeline pipeline,
    Matrix3x2f pose,
    ScreenRect bounds,
    ScreenRect scissorArea
) implements SimpleGuiElementRenderState {

    @Override
    public void setupVertices(VertexConsumer buffer) {
        var colorArgb = this.color.argb();

        var begin = new Vector2d(this.bounds.getLeft(), this.bounds.getTop());
        var end = new Vector2d(this.bounds.getRight(), this.bounds.getBottom());

        var step = end.sub(begin, new Vector2d()).normalize().mul(this.segmentLength);
        var segmentCount = (int) ((end.distance(begin) + this.segmentLength) / (this.segmentLength * 2));

        var offset = end.sub(begin, new Vector2d()).perpendicular().normalize().mul(this.thiccness * .5d);
        end.set(begin).add(step);

        step.mul(2);

        for (var i = 0; i < segmentCount; i++) {
            buffer.vertex(this.pose, (float) (begin.x + offset.x), (float) (begin.y + offset.y)).color(colorArgb);
            buffer.vertex(this.pose, (float) (begin.x - offset.x), (float) (begin.y - offset.y)).color(colorArgb);
            buffer.vertex(this.pose, (float) (end.x - offset.x), (float) (end.y - offset.y)).color(colorArgb);
            buffer.vertex(this.pose, (float) (end.x + offset.x), (float) (end.y + offset.y)).color(colorArgb);

            begin.add(step);
            end.add(step);
        }
    }

    @Override
    public RenderPipeline pipeline() {
        return this.pipeline;
    }

    @Override
    public TextureSetup textureSetup() {
        return TextureSetup.empty();
    }

    @Override
    public ScreenRect scissorArea() {
        return this.scissorArea;
    }

    @Override
    public ScreenRect bounds() {
        var bounds = this.bounds.transformEachVertex(this.pose);
        return this.scissorArea != null ? this.scissorArea.intersection(bounds) : bounds;
    }
}
