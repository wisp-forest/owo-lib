package io.wispforest.owo.ui.core;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import org.jetbrains.annotations.ApiStatus;

public final class OwoUIPipelines {

    public static final RenderPipeline.Snippet HSV_SNIPPET = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
        .withVertexShader(Identifier.withDefaultNamespace("core/gui"))
        .withFragmentShader(Identifier.fromNamespaceAndPath("owo", "core/spectrum"))
        .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
        .withBlend(BlendFunction.TRANSLUCENT)
        .buildSnippet();

    public static final RenderPipeline GUI_HSV = RenderPipeline.builder(HSV_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath("owo", "pipeline/gui_hsv"))
        .build();

    public static final RenderPipeline GUI_BLUR = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath("owo", "pipeline/gui_blur"))
        .withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS)
        .withVertexShader(Identifier.fromNamespaceAndPath("owo", "core/blur"))
        .withFragmentShader(Identifier.fromNamespaceAndPath("owo", "core/blur"))
        .withSampler("InputSampler")
        .withUniform("BlurSettings", UniformType.UNIFORM_BUFFER)
        .build();

    public static final RenderPipeline GUI_TRIANGLE_FAN = RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath("owo", "pipeline/gui_triangle_fan"))
        .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_FAN)
        .build();

    public static final RenderPipeline GUI_TRIANGLE_STRIP = RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath("owo", "pipeline/gui_triangle_strip"))
        .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP)
        .build();

    public static final RenderPipeline GUI_TEXTURED_NO_BLEND = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath("owo", "pipeline/gui_textured"))
        .withoutBlend()
        .build();

    @ApiStatus.Internal
    public static void register(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(GUI_HSV);
        event.registerPipeline(GUI_BLUR);
        event.registerPipeline(GUI_TRIANGLE_FAN);
        event.registerPipeline(GUI_TRIANGLE_STRIP);
        event.registerPipeline(GUI_TEXTURED_NO_BLEND);
    }
}
