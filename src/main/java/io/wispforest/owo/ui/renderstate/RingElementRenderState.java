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

public record RingElementRenderState(
    RenderPipeline pipeline,
    Matrix3x2f pose,
    ScreenRect scissorArea,
    int centerX,
    int centerY,
    double angleFrom,
    double angleTo,
    int segments,
    double innerRadius,
    double outerRadius,
    Color innerColor,
    Color outerColor
) implements SimpleGuiElementRenderState {
    @Override
    public void setupVertices(VertexConsumer vertices, float depth) {
        double angleStep = Math.toRadians(this.angleTo - this.angleFrom) / this.segments;
        int inColor = this.innerColor.argb();
        int outColor = this.outerColor.argb();

        for (int i = 0; i <= this.segments; i++) {
            double theta = Math.toRadians(this.angleFrom) + i * angleStep;

            vertices.vertex(this.pose, (float) (this.centerX - Math.cos(theta) * this.outerRadius), (float) (this.centerY - Math.sin(theta) * this.outerRadius), depth)
                .color(outColor);
            vertices.vertex(this.pose, (float) (this.centerX - Math.cos(theta) * this.innerRadius), (float) (this.centerY - Math.sin(theta) * this.innerRadius), depth)
                .color(inColor);
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
            new ScreenPos((int) (this.centerX - this.outerRadius), (int) (this.centerY - this.outerRadius)),
            (int) Math.ceil(this.outerRadius * 2),
            (int) Math.ceil(this.outerRadius * 2)
        ).transformEachVertex(this.pose);

        return this.scissorArea != null ? this.scissorArea.intersection(screenRect) : screenRect;
    }
}
