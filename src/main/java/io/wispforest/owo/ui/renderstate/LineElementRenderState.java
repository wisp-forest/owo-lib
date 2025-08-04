package io.wispforest.owo.ui.renderstate;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import org.joml.Matrix3x2f;
import org.joml.Vector2d;

public record LineElementRenderState(
    RenderPipeline pipeline,
    Matrix3x2f pose,
    ScreenRect scissorArea,
    int x0,
    int y0,
    int x1,
    int y1,
    double thiccness,
    Color color
) implements SimpleGuiElementRenderState {
    @Override
    public void setupVertices(VertexConsumer vertices, float depth) {
        var offset = new Vector2d(this.x1 - this.x0, this.y1 - this.y0).perpendicular().normalize().mul(this.thiccness * .5d);

        int vColor = this.color.argb();
        vertices.vertex(this.pose, (float) (x0 + offset.x), (float) (y0 + offset.y), depth).color(vColor);
        vertices.vertex(this.pose, (float) (x0 - offset.x), (float) (y0 - offset.y), depth).color(vColor);
        vertices.vertex(this.pose, (float) (x1 - offset.x), (float) (y1 - offset.y), depth).color(vColor);
        vertices.vertex(this.pose, (float) (x1 + offset.x), (float) (y1 + offset.y), depth).color(vColor);
    }

    @Override
    public TextureSetup textureSetup() {
        return TextureSetup.empty();
    }

    @Override
    public ScreenRect bounds() {
        return new ScreenRect(this.x0, this.y0, this.x1 - this.x0, this.y1 - this.y0);
    }
}
