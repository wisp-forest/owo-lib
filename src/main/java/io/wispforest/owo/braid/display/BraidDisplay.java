package io.wispforest.owo.braid.display;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.Owo;
import io.wispforest.owo.braid.core.AppState;
import io.wispforest.owo.braid.core.EventBinding;
import io.wispforest.owo.braid.core.TextureSurface;
import io.wispforest.owo.braid.framework.widget.Widget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
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
            client.getRenderTickCounter().getDynamicDeltaTicks()
        );

        this.app.draw(this.surface.guiRenderer.newDrawContext());
    }

    public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light) {
        var layer = RENDER_TYPE.apply(this.surface);
        queue.submitCustom(matrices, layer, (matricesEntry, buffer) -> {
            var normal = this.quad.normal.toVector3f();
            buffer.vertex(matricesEntry, 0, 0, 0).color(1f, 1f, 1f, 1f).texture(0, 1).light(light).normal(matricesEntry, normal);
            buffer.vertex(matricesEntry, this.quad.left.toVector3f()).color(1f, 1f, 1f, 1f).texture(0, 0).light(light).normal(matricesEntry, normal);
            buffer.vertex(matricesEntry, this.quad.top.add(this.quad.left).toVector3f()).color(1f, 1f, 1f, 1f).texture(1, 0).light(light).normal(matricesEntry, normal);
            buffer.vertex(matricesEntry, this.quad.top.toVector3f()).color(1f, 1f, 1f, 1f).texture(1, 1).light(light).normal(matricesEntry, normal);
        });
    }

    // ---

    public static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.TERRAIN_SNIPPET)
        .withLocation(Owo.id("pipeline/braid_display"))
        .withShaderDefine("ALPHA_CUTOUT", 0.1F)
        .withCull(false)
        .withBlend(BlendFunction.TRANSLUCENT)
        .build();

    private static final Function<TextureSurface, RenderLayer> RENDER_TYPE = surface -> RenderLayer.of(
        Owo.id("braid_display").toString(),
        16384,
        PIPELINE,
        RenderLayer.MultiPhaseParameters.builder()
            .texture(new SurfaceTexture(surface))
            .lightmap(RenderPhase.ENABLE_LIGHTMAP)
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
