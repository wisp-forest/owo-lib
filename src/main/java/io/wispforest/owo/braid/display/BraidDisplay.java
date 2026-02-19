package io.wispforest.owo.braid.display;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.PoseStack;
import io.wispforest.owo.Owo;
import io.wispforest.owo.braid.core.AppState;
import io.wispforest.owo.braid.core.EventBinding;
import io.wispforest.owo.braid.core.TextureSurface;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.mixin.braid.RenderTypeInvoker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
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
            Minecraft.getInstance(),
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
            client.getDeltaTracker().getGameTimeDeltaTicks()
        );

        this.app.draw(this.surface.guiRenderer.newGraphics(this.app.cursorPosition().x(), this.app.cursorPosition().y()));
    }

    public void render(PoseStack matrices, SubmitNodeCollector queue, int light) {
        var layer = RENDER_TYPE.apply(this.surface);
        queue.submitCustomGeometry(matrices, layer, (matricesEntry, buffer) -> {
            var normal = this.quad.normal.toVector3f();
            buffer.addVertex(matricesEntry, 0, 0, 0).setColor(1f, 1f, 1f, 1f).setUv(0, 1).setLight(light).setNormal(matricesEntry, normal);
            buffer.addVertex(matricesEntry, this.quad.left.toVector3f()).setColor(1f, 1f, 1f, 1f).setUv(0, 0).setLight(light).setNormal(matricesEntry, normal);
            buffer.addVertex(matricesEntry, this.quad.top.add(this.quad.left).toVector3f()).setColor(1f, 1f, 1f, 1f).setUv(1, 0).setLight(light).setNormal(matricesEntry, normal);
            buffer.addVertex(matricesEntry, this.quad.top.toVector3f()).setColor(1f, 1f, 1f, 1f).setUv(1, 1).setLight(light).setNormal(matricesEntry, normal);
        });
    }

    // ---

    public static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.BLOCK_SNIPPET)
        .withLocation(Owo.id("pipeline/braid_display"))
        .withShaderDefine("ALPHA_CUTOUT", 0.1F)
        .withCull(false)
        .withBlend(BlendFunction.TRANSLUCENT)
        .build();

    private static final Function<TextureSurface, RenderType> RENDER_TYPE = surface -> RenderTypeInvoker.owo$of(
        Owo.id("braid_display").toString(),
        RenderSetup.builder(PIPELINE)
            .withTexture("Sampler0", surface.registeredTextureId)
            .useLightmap()
            .createRenderSetup()
    );
}
