package io.wispforest.owo.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.wispforest.owo.ui.core.OwoUIPipelines;
import io.wispforest.owo.ui.core.PositionedRectangle;
import io.wispforest.owo.ui.event.WindowResizeCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.BufferAllocator;
import org.jetbrains.annotations.ApiStatus;

import java.util.OptionalDouble;
import java.util.OptionalInt;

/// A simple utility to blur an area of the screen with configurable strength
/// and quality. Look at [#blur(float,float)] for a
/// reference implementation
public class OwoBlurRenderer {

    private static Framebuffer input;

    @ApiStatus.Internal
    public static void initialize(MinecraftClient client) {
        var window = client.getWindow();
        input = new SimpleFramebuffer("owo_blur_input", window.getFramebufferWidth(), window.getFramebufferHeight(), false);
        WindowResizeCallback.EVENT.register((innerClient, innerWindow) -> {
            if (input == null) return;
            input.resize(innerWindow.getFramebufferWidth(), innerWindow.getFramebufferHeight());
        });
    }

    public static void drawBlur(DrawContext context, PositionedRectangle area, int directions, float quality, float size) {
        RenderSystem.getDevice().createCommandEncoder()
            .copyTextureToTexture(
                MinecraftClient.getInstance().getFramebuffer().getColorAttachment(), input.getColorAttachment(),
                0, 0, 0, 0, 0, input.textureWidth, input.textureHeight
            );

        try (var allocator = new BufferAllocator(VertexFormats.POSITION.getVertexSize() * 4)) {
            var matrix = context.getMatrices().peek().getPositionMatrix();
            var bufferBuilder = new BufferBuilder(allocator, VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);

            bufferBuilder.vertex(matrix, area.x(), area.y(), 0);
            bufferBuilder.vertex(matrix, area.x(), area.y() + area.height(), 0);
            bufferBuilder.vertex(matrix, area.x() + area.width(), area.y() + area.height(), 0);
            bufferBuilder.vertex(matrix, area.x() + area.width(), area.y(), 0);

            try (var buffer = bufferBuilder.end()) {
                var vertexBuffer = VertexFormats.POSITION.uploadImmediateVertexBuffer(buffer.getBuffer());
                var indexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.DrawMode.QUADS).getIndexBuffer(buffer.getDrawParameters().indexCount());
                try (var renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                    MinecraftClient.getInstance().getFramebuffer().getColorAttachment(),
                    OptionalInt.empty(),
                    MinecraftClient.getInstance().getFramebuffer().getDepthAttachment(),
                    OptionalDouble.empty()
                )) {
                    renderPass.setPipeline(OwoUIPipelines.GUI_BLUR);
                    renderPass.setVertexBuffer(0, vertexBuffer);
                    renderPass.setIndexBuffer(indexBuffer, RenderSystem.getSequentialBuffer(VertexFormat.DrawMode.QUADS).getIndexType());

                    renderPass.bindSampler("InputSampler", input.getColorAttachment());
                    renderPass.setUniform("InputResolution", (float) input.textureWidth, (float) input.textureHeight);
                    renderPass.setUniform("Directions", (float) directions);
                    renderPass.setUniform("Quality", quality);
                    renderPass.setUniform("Size", size);

                    renderPass.drawIndexed(0, buffer.getDrawParameters().indexCount());
                }
            }
        }
    }
}