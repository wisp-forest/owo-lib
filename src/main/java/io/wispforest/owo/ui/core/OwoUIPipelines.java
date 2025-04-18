package io.wispforest.owo.ui.core;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

public final class OwoUIPipelines {

    public static final RenderPipeline.Snippet HSV_SNIPPET = RenderPipeline.builder(RenderPipelines.MATRICES_COLOR_SNIPPET)
        .withVertexShader(Identifier.ofVanilla("core/position_color"))
        .withFragmentShader(Identifier.of("owo", "core/spectrum"))
        .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS)
        .withBlend(BlendFunction.TRANSLUCENT)
        .buildSnippet();

    public static final RenderPipeline GUI_HSV = RenderPipeline.builder(HSV_SNIPPET)
        .withLocation(Identifier.of("owo", "pipeline/gui_hsv"))
        .build();

    public static final RenderPipeline GUI_BLUR = RenderPipeline.builder(RenderPipelines.MATRICES_COLOR_SNIPPET)
        .withLocation(Identifier.of("owo", "pipeline/gui_blur"))
        .withVertexFormat(VertexFormats.POSITION, VertexFormat.DrawMode.QUADS)
        .withVertexShader(Identifier.ofVanilla("core/position"))
        .withFragmentShader(Identifier.of("owo", "core/blur"))
        .withSampler("InputSampler")
        .withUniform("InputResolution", UniformType.VEC2)
        .withUniform("Directions", UniformType.FLOAT)
        .withUniform("Quality", UniformType.FLOAT)
        .withUniform("Size", UniformType.FLOAT)
        .build();

    public static final RenderPipeline GUI_TRIANGLE_FAN = RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
        .withLocation(Identifier.of("owo", "pipeline/gui_triangle_fan"))
        .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.TRIANGLE_FAN)
        .build();

    public static final RenderPipeline GUI_TRIANGLE_STRIP = RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
        .withLocation(Identifier.of("owo", "pipeline/gui_triangle_strip"))
        .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.TRIANGLE_STRIP)
        .build();

    public static final RenderPipeline GUI_TEXTURED_NO_BLEND = RenderPipeline.builder(RenderPipelines.POSITION_TEX_COLOR_SNIPPET)
        .withLocation("pipeline/gui_textured")
        .withoutBlend()
        .build();

    @ApiStatus.Internal
    public static void register() {
        RenderPipelines.register(GUI_HSV);
        RenderPipelines.register(GUI_BLUR);
        RenderPipelines.register(GUI_TRIANGLE_FAN);
        RenderPipelines.register(GUI_TRIANGLE_STRIP);
        RenderPipelines.register(GUI_TEXTURED_NO_BLEND);
    }
}
