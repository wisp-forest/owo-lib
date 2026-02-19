package io.wispforest.owo.ui.renderstate;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record CircleElementRenderState(
    RenderPipeline pipeline,
    Matrix3x2f pose,
    ScreenRectangle scissorArea,
    int centerX,
    int centerY,
    double angleFrom,
    double angleTo,
    int segments,
    double radius,
    Color color
) implements GuiElementRenderState {
    @Override
    public void buildVertices(VertexConsumer vertices) {
        double angleStep = Math.toRadians(this.angleTo - this.angleFrom) / this.segments;
        int vColor = this.color.argb();

        vertices.addVertexWith2DPose(this.pose, this.centerX, this.centerY).setColor(vColor);

        for (int i = this.segments; i >= 0; i--) {
            double theta = Math.toRadians(this.angleFrom) + i * angleStep;
            vertices.addVertexWith2DPose(this.pose, (float) (this.centerX - Math.cos(theta) * this.radius), (float) (this.centerY - Math.sin(theta) * this.radius))
                .setColor(vColor);
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
    public @Nullable ScreenRectangle scissorArea() {
        return this.scissorArea;
    }

    @Override
    public ScreenRectangle bounds() {
        var screenRect =  new ScreenRectangle(
            new ScreenPosition((int) (this.centerX - this.radius), (int) (this.centerY - this.radius)),
            (int) Math.ceil(this.radius * 2),
            (int) Math.ceil(this.radius * 2)
        ).transformMaxBounds(this.pose);

        return this.scissorArea != null ? this.scissorArea.intersection(screenRect) : screenRect;
    }
}
