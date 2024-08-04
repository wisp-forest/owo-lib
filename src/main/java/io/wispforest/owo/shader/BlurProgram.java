package io.wispforest.owo.shader;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.event.WindowResizeCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.opengl.GL30;

/**
 * A simple GlProgram implementation that applies an approximate gaussian
 * blur to the quads it draws. Look at {@link Surface#blur(float, float)} for a
 * reference implementation
 */
public class BlurProgram extends GlProgram {

    private Uniform inputResolution;
    private Uniform directions;
    private Uniform quality;
    private Uniform size;
    private RenderTarget input;

    public BlurProgram() {
        super(Identifier.of("owo", "blur"), DefaultVertexFormat.POSITION);

        WindowResizeCallback.EVENT.register((client, window) -> {
            if (this.input == null) return;
            this.input.resize(window.getWidth(), window.getHeight(), Minecraft.ON_OSX);
        });
    }

    public void setParameters(int directions, float quality, float size) {
        this.directions.set((float) directions);
        this.size.set(size);
        this.quality.set(quality);
    }

    @Override
    public void use() {
        var buffer = Minecraft.getInstance().getMainRenderTarget();

        this.input.bindWrite(false);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, buffer.frameBufferId);
        GL30.glBlitFramebuffer(0, 0, buffer.width, buffer.height, 0, 0, buffer.width, buffer.height, GL30.GL_COLOR_BUFFER_BIT, GL30.GL_LINEAR);
        buffer.bindWrite(false);

        this.inputResolution.set((float) buffer.width, (float) buffer.height);
        this.backingProgram.setSampler("InputSampler", this.input.getColorTextureId());

        super.use();
    }

    @Override
    protected void setup() {
        this.inputResolution = this.findUniform("InputResolution");
        this.directions = this.findUniform("Directions");
        this.quality = this.findUniform("Quality");
        this.size = this.findUniform("Size");

        var window = Minecraft.getInstance().getWindow();
        this.input = new TextureTarget(window.getWidth(), window.getHeight(), false, Minecraft.ON_OSX);
    }
}