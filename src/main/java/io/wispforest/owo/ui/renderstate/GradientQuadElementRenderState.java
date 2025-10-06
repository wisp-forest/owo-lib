package io.wispforest.owo.ui.renderstate;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.OwoUIPipelines;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record GradientQuadElementRenderState(
    RenderPipeline pipeline,
    Matrix3x2f pose,
    ScreenRect bounds,
    ScreenRect scissorArea,
    Color colorTL,
    Color colorTR,
    Color colorBL,
    Color colorBR
) implements SimpleGuiElementRenderState {

    @Override
    public void setupVertices(VertexConsumer vertices) {
        vertices.vertex(this.pose(), (float) this.bounds.getLeft(), (float) this.bounds.getTop()).color(this.colorTL.argb());
        vertices.vertex(this.pose(), (float) this.bounds.getLeft(), (float) this.bounds.getBottom()).color(this.colorBL.argb());
        vertices.vertex(this.pose(), (float) this.bounds.getRight(), (float) this.bounds.getBottom()).color(this.colorBR.argb());
        vertices.vertex(this.pose(), (float) this.bounds.getRight(), (float) this.bounds.getTop()).color(this.colorTR.argb());
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
    public @Nullable ScreenRect bounds() {
        return this.scissorArea != null ? this.scissorArea.intersection(this.bounds) : this.bounds;
    }
}
