package io.wispforest.owo.braid.core;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.mixin.ui.access.RenderSystemAccessor;
import io.wispforest.owo.ui.util.ScissorStack;
import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.EventStream;
import io.wispforest.owo.util.FramebufferOverride;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.DiffuseLighting;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL32;

public class TextureSurface implements Surface {

    private final SimpleFramebuffer framebuffer;
    private final EventStream<ResizeCallback> resizeEvents = ResizeCallback.newStream();

    private CursorStyle currentCursorStyle = CursorStyle.NONE;

    public TextureSurface(int width, int height) {
        this.framebuffer = createFramebufferAndRestoreState(width, height, true);
    }

    public void resize(int width, int height) {
        this.framebuffer.resize(width, height, MinecraftClient.IS_SYSTEM_MAC);
        this.resizeEvents.sink().onResize(width, height);
    }

    public int texture() {
        return this.framebuffer.getColorAttachment();
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

    public static SimpleFramebuffer createFramebufferAndRestoreState(int width, int height, boolean useDepth) {
        // TODO: consider where this makes sense to have here.
        //       it probably makes more sense to "enforce" surface
        //       happens when it's safe to do so (i.e. at the
        //       start/end of a frame) or on-demand during drawing
        var previousFramebuffer = GlStateManager.getBoundFramebuffer();

        var viewportX = GlStateManager.Viewport.getX();
        var viewportY = GlStateManager.Viewport.getY();
        var viewportWidth = GlStateManager.Viewport.getWidth();
        var viewportHeight = GlStateManager.Viewport.getHeight();

        var framebuffer = new SimpleFramebuffer(width, height, useDepth, MinecraftClient.IS_SYSTEM_MAC);

        GlStateManager._glBindFramebuffer(GL32.GL_FRAMEBUFFER, previousFramebuffer);
        GlStateManager._viewport(viewportX, viewportY, viewportWidth, viewportHeight);

        return framebuffer;
    }

    // ---

    private Matrix4f projectionBackup;
    private VertexSorter vertexSorterBackup;
    private float fogBackup;
    private final Vector3f[] lightingBackup = new Vector3f[2];

    @Override
    public void beginRendering() {
        this.framebuffer.beginWrite(true);
        FramebufferOverride.push(this.framebuffer);

        ScissorStack.pushViewportDimensions(() -> new ScissorStack.ViewportDimensions(1, this.width(), this.height(), this.width(), this.height()));

        RenderSystem.clearColor(0f, 0f, 0f, 0f);
        RenderSystem.clear(GL32.GL_COLOR_BUFFER_BIT | GL32.GL_DEPTH_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);

        this.projectionBackup = new Matrix4f(RenderSystem.getProjectionMatrix());
        this.vertexSorterBackup = RenderSystem.getVertexSorting();

        var projection = new Matrix4f().setOrtho(0, this.width(), this.height(), 0, 1000, 21000);
        RenderSystem.setProjectionMatrix(projection, VertexSorter.BY_Z);

        var modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.identity();
        modelViewStack.translate(0, 0, -11000);

        this.fogBackup = RenderSystem.getShaderFogStart();
        RenderSystem.setShaderFogStart(Float.MAX_VALUE);

        this.lightingBackup[0] = new Vector3f(RenderSystemAccessor.owo$getShaderLightDirections()[0]);
        this.lightingBackup[1] = new Vector3f(RenderSystemAccessor.owo$getShaderLightDirections()[1]);
        DiffuseLighting.enableGuiDepthLighting();
    }

    @Override
    public void endRendering() {
        RenderSystem.setShaderLights(this.lightingBackup[0], this.lightingBackup[1]);
        RenderSystem.setShaderFogStart(this.fogBackup);

        ScissorStack.popViewportDimensions();
        FramebufferOverride.pop();

        var activeFramebuffer = FramebufferOverride.top();
        if (activeFramebuffer == null) {
            activeFramebuffer = MinecraftClient.getInstance().getFramebuffer();
        }

        activeFramebuffer.beginWrite(true);

        RenderSystem.getModelViewStack().popMatrix();

        RenderSystem.setProjectionMatrix(this.projectionBackup, this.vertexSorterBackup);
        this.projectionBackup = null;
    }

    @Override
    public void dispose() {
        this.framebuffer.delete();
    }
}
