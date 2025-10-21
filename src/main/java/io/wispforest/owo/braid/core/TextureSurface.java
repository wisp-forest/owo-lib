package io.wispforest.owo.braid.core;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.util.BraidGuiRendererTargetOverride;
import io.wispforest.owo.mixin.braid.GameRendererAccessor;
import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.EventStream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.fog.FogRenderer;

public class TextureSurface implements Surface {

    private final SimpleFramebuffer framebuffer;
    private final EventStream<ResizeCallback> resizeEvents = ResizeCallback.newStream();

    private CursorStyle currentCursorStyle = CursorStyle.NONE;

    public TextureSurface(int width, int height) {
        this.framebuffer = new SimpleFramebuffer("texture surface", width, height, true);
    }

    public void resize(int width, int height) {
        this.framebuffer.resize(width, height);
        this.resizeEvents.sink().onResize(width, height);
    }

    public GpuTextureView texture() {
        return this.framebuffer.getColorAttachmentView();
    }

    @Override
    public int width() {
        return this.framebuffer.textureWidth;
    }

    @Override
    public int height() {
        return this.framebuffer.textureHeight;
    }

    @Override
    public double scaleFactor() {
        return 1;
    }

    @Override
    public EventSource<ResizeCallback> onResize() {
        return this.resizeEvents.source();
    }

    @Override
    public CursorStyle currentCursorStyle() {
        return this.currentCursorStyle;
    }

    @Override
    public void setCursorStyle(CursorStyle style) {
        this.currentCursorStyle = style;
    }

    // ---

    @Override
    public void beginRendering() {
        RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(
            this.framebuffer.getColorAttachment(),
            0x00000000,
            this.framebuffer.getDepthAttachment(),
            1
        );
    }

    @Override
    public void endRendering() {
        BraidGuiRendererTargetOverride.run(
            new BraidGuiRendererTargetOverride(
                this.framebuffer,
                this
            ),
            () -> {
                var gameRenderer = (GameRendererAccessor) MinecraftClient.getInstance().gameRenderer;
                gameRenderer.owo$getGuiRenderer().render(gameRenderer.owo$getFogRenderer().getFogBuffer(FogRenderer.FogType.NONE));
            }
        );
    }

    @Override
    public void dispose() {
        this.framebuffer.delete();
    }
}
