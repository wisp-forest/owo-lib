package io.wispforest.owo.ui.renderstate;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.client.gui.ScreenPos;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record CircleElementRenderState(
    RenderPipeline pipeline,
    Matrix3x2f pose,
    ScreenRect scissorArea,
    int centerX,
    int centerY,
    double angleFrom,
    double angleTo,
    int segments,
    double radius,
    Color color
) implements SimpleGuiElementRenderState {
    @Override
    public void setupVertices(VertexConsumer vertices) {
        double angleStep = Math.toRadians(this.angleTo - this.angleFrom) / this.segments;
        int vColor = this.color.argb();

        vertices.vertex(this.pose, this.centerX, this.centerY).color(vColor);

        for (int i = this.segments; i >= 0; i--) {
            double theta = Math.toRadians(this.angleFrom) + i * angleStep;
            vertices.vertex(this.pose, (float) (this.centerX - Math.cos(theta) * this.radius), (float) (this.centerY - Math.sin(theta) * this.radius))
                .color(vColor);
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
    public @Nullable ScreenRect scissorArea() {
        return this.scissorArea;
    }

    @Override
    public ScreenRect bounds() {
        var screenRect =  new ScreenRect(
            new ScreenPos((int) (this.centerX - this.radius), (int) (this.centerY - this.radius)),
            (int) Math.ceil(this.radius * 2),
            (int) Math.ceil(this.radius * 2)
        ).transformEachVertex(this.pose);

        return this.scissorArea != null ? this.scissorArea.intersection(screenRect) : screenRect;
    }
}
