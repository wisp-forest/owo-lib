package io.wispforest.owo.braid.core.element;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.wispforest.owo.braid.core.Color;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import org.joml.Matrix3x2f;
import org.joml.Vector2d;

public record BraidDashedLineElement(
    Color color,
    double thiccness,
    double segmentLength,
    RenderPipeline pipeline,
    Matrix3x2f pose,
    ScreenRectangle bounds,
    ScreenRectangle scissorArea
) implements GuiElementRenderState {

    @Override
    public void buildVertices(VertexConsumer buffer) {
        var colorArgb = this.color.argb();

        var begin = new Vector2d(this.bounds.left(), this.bounds.top());
        var end = new Vector2d(this.bounds.right(), this.bounds.bottom());

        var step = end.sub(begin, new Vector2d()).normalize().mul(this.segmentLength);
        var segmentCount = (int) ((end.distance(begin) + this.segmentLength) / (this.segmentLength * 2));

        var offset = end.sub(begin, new Vector2d()).perpendicular().normalize().mul(this.thiccness * .5d);
        end.set(begin).add(step);

        step.mul(2);

        for (var i = 0; i < segmentCount; i++) {
            buffer.addVertexWith2DPose(this.pose, (float) (begin.x + offset.x), (float) (begin.y + offset.y)).setColor(colorArgb);
            buffer.addVertexWith2DPose(this.pose, (float) (begin.x - offset.x), (float) (begin.y - offset.y)).setColor(colorArgb);
            buffer.addVertexWith2DPose(this.pose, (float) (end.x - offset.x), (float) (end.y - offset.y)).setColor(colorArgb);
            buffer.addVertexWith2DPose(this.pose, (float) (end.x + offset.x), (float) (end.y + offset.y)).setColor(colorArgb);

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
        return TextureSetup.noTexture();
    }

    @Override
    public ScreenRectangle scissorArea() {
        return this.scissorArea;
    }

    @Override
    public ScreenRectangle bounds() {
        var bounds = this.bounds.transformMaxBounds(this.pose);
        return this.scissorArea != null ? this.scissorArea.intersection(bounds) : bounds;
    }
}
