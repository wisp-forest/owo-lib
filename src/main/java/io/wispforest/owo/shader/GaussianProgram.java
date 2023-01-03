package io.wispforest.owo.shader;

import io.wispforest.owo.ui.event.WindowResizeCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL30;

public class GaussianProgram extends GlProgram {

    private GlUniform resolution;
    private Framebuffer input;

    public GaussianProgram() {
        super(new Identifier("owo", "gaussian"), VertexFormats.POSITION);

        WindowResizeCallback.EVENT.register((client, window) -> {
            if (this.input == null) return;
            this.input.resize(window.getFramebufferWidth(), window.getFramebufferHeight(), MinecraftClient.IS_SYSTEM_MAC);
        });
    }

    @Override
    public void use() {
        var buffer = MinecraftClient.getInstance().getFramebuffer();

        this.input.beginWrite(false);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, buffer.fbo);
        GL30.glBlitFramebuffer(0, 0, buffer.textureWidth, buffer.textureHeight, 0, 0, buffer.textureWidth, buffer.textureHeight, GL30.GL_COLOR_BUFFER_BIT, GL30.GL_LINEAR);
        buffer.beginWrite(false);

        this.resolution.set((float) buffer.textureWidth, (float) buffer.textureHeight);
        this.backingProgram.addSampler("DiffuseSampler", this.input.getColorAttachment());

        super.use();
    }

    @Override
    protected void setup() {
        this.resolution = this.findUniform("Resolution");

        var window = MinecraftClient.getInstance().getWindow();
        this.input = new SimpleFramebuffer(window.getFramebufferWidth(), window.getFramebufferHeight(), false, MinecraftClient.IS_SYSTEM_MAC);
    }
}