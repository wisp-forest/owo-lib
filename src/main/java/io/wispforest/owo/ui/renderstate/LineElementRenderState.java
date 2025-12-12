package io.wispforest.owo.ui.renderstate;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import org.joml.Matrix3x2f;
import org.joml.Vector2d;

public record LineElementRenderState(
    RenderPipeline pipeline,
    Matrix3x2f pose,
    ScreenRectangle scissorArea,
    int x0,
    int y0,
    int x1,
    int y1,
    double thiccness,
    Color color
) implements GuiElementRenderState {
    @Override
    public void buildVertices(VertexConsumer vertices) {
        var offset = new Vector2d(this.x1 - this.x0, this.y1 - this.y0).perpendicular().normalize().mul(this.thiccness * .5d);

        int vColor = this.color.argb();
        vertices.addVertexWith2DPose(this.pose, (float) (x0 + offset.x), (float) (y0 + offset.y)).setColor(vColor);
        vertices.addVertexWith2DPose(this.pose, (float) (x0 - offset.x), (float) (y0 - offset.y)).setColor(vColor);
        vertices.addVertexWith2DPose(this.pose, (float) (x1 - offset.x), (float) (y1 - offset.y)).setColor(vColor);
        vertices.addVertexWith2DPose(this.pose, (float) (x1 + offset.x), (float) (y1 + offset.y)).setColor(vColor);
    }

    @Override
    public TextureSetup textureSetup() {
        return TextureSetup.noTexture();
    }

    @Override
    public ScreenRectangle bounds() {
        return new ScreenRectangle(this.x0, this.y0, this.x1 - this.x0, this.y1 - this.y0);
    }
}
