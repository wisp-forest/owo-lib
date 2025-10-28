package io.wispforest.owo.braid.display;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.Owo;
import io.wispforest.owo.braid.core.AppState;
import io.wispforest.owo.braid.core.EventBinding;
import io.wispforest.owo.braid.core.TextureSurface;
import io.wispforest.owo.braid.framework.widget.Widget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Function;

public class BraidDisplay {

    public DisplayQuad quad;

    public final AppState app;
    public final TextureSurface surface;

    @ApiStatus.Internal
    public boolean primaryPressed = false;
    @ApiStatus.Internal
    public boolean secondaryPressed = false;

    boolean renderAutomatically = false;

    public BraidDisplay(DisplayQuad quad, int surfaceWidth, int surfaceHeight, Widget widget) {
        this.quad = quad;
        this.surface = new TextureSurface(surfaceWidth, surfaceHeight);
        this.app = new AppState(
            null,
            AppState.formatName("BraidDisplay", widget),
            MinecraftClient.getInstance(),
            this.surface,
            new EventBinding.Headless(),
            widget
        );
    }

    public BraidDisplay renderAutomatically() {
        this.renderAutomatically = true;
        return this;
    }

    public void updateAndDrawApp() {
        var client = this.app.client();

        this.app.processEvents(
            client.getRenderTickCounter().getLastFrameDuration()
        );

        var ctx = new DrawContext(client, client.getBufferBuilders().getEntityVertexConsumers());
        ctx.translate(0, 0, -11000);

        this.app.draw(ctx);
    }

    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        var layer = RENDER_TYPE.apply(this.surface);

        var buffer = vertexConsumers.getBuffer(layer);

        var matrixFrame = matrices.peek();
        var normal = this.quad.normal.toVector3f();

        buffer.vertex(matrixFrame, 0, 0, 0).color(1f, 1f, 1f, 1f).texture(0, 1).light(light).normal(matrixFrame, normal.x, normal.y, normal.z);
        buffer.vertex(matrixFrame, this.quad.left.toVector3f()).color(1f, 1f, 1f, 1f).texture(0, 0).light(light).normal(matrixFrame, normal.x, normal.y, normal.z);
        buffer.vertex(matrixFrame, this.quad.top.add(this.quad.left).toVector3f()).color(1f, 1f, 1f, 1f).texture(1, 0).light(light).normal(matrixFrame, normal.x, normal.y, normal.z);
        buffer.vertex(matrixFrame, this.quad.top.toVector3f()).color(1f, 1f, 1f, 1f).texture(1, 1).light(light).normal(matrixFrame, normal.x, normal.y, normal.z);
    }

    // ---
    private static final Function<TextureSurface, RenderLayer> RENDER_TYPE = surface -> RenderLayer.of(
        Owo.id("braid_display").toString(),
        VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL,
        VertexFormat.DrawMode.QUADS,
        16384,
        RenderLayer.MultiPhaseParameters.builder()
            .texture(new SurfaceTexture(surface))
            .program(RenderPhase.CUTOUT_PROGRAM)
            .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
            .lightmap(RenderPhase.ENABLE_LIGHTMAP)
            .cull(RenderPhase.DISABLE_CULLING)
            .build(false)
    );

    private static class SurfaceTexture extends RenderPhase.TextureBase {
        public SurfaceTexture(TextureSurface surface) {
            super(
                () -> RenderSystem.setShaderTexture(0, surface.texture()),
                () -> {}
            );
        }
    }
}
