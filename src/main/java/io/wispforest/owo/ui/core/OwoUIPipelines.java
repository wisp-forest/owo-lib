package io.wispforest.owo.ui.core;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

public final class OwoUIPipelines {

    private static final RenderPipeline.Snippet MATRICES_PROJECTION_SNIPPET = RenderPipeline.builder(new RenderPipeline.Snippet[0])
        .withBindGroupLayout(BindGroupLayouts.GLOBALS)
        .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
        .buildSnippet();

    public static final RenderPipeline.Snippet HSV_SNIPPET = RenderPipeline.builder(MATRICES_PROJECTION_SNIPPET)
        .withVertexShader(Identifier.withDefaultNamespace("core/gui"))
        .withFragmentShader(Identifier.fromNamespaceAndPath("owo", "core/spectrum"))
        .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
        .withPrimitiveTopology(PrimitiveTopology.QUADS)
        .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
        .buildSnippet();

    public static final RenderPipeline GUI_HSV = RenderPipeline.builder(HSV_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath("owo", "pipeline/gui_hsv"))
        .build();

    public static final RenderPipeline GUI_BLUR = RenderPipeline.builder(MATRICES_PROJECTION_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath("owo", "pipeline/gui_blur"))
        .withVertexBinding(0, DefaultVertexFormat.POSITION)
        .withPrimitiveTopology(PrimitiveTopology.QUADS)
        .withVertexShader(Identifier.fromNamespaceAndPath("owo", "core/blur"))
        .withFragmentShader(Identifier.fromNamespaceAndPath("owo", "core/blur"))
        .build();

    private static final RenderPipeline.Snippet GUI_SNIPPET = RenderPipeline.builder(new RenderPipeline.Snippet[0])
        .withBindGroupLayout(BindGroupLayouts.GLOBALS)
        .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
        .withVertexShader("core/gui")
        .withFragmentShader("core/gui")
        .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
        .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
        .withPrimitiveTopology(PrimitiveTopology.QUADS)
        .buildSnippet();

    public static final RenderPipeline GUI_TRIANGLE_FAN = RenderPipeline.builder(GUI_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath("owo", "pipeline/gui_triangle_fan"))
        .withPrimitiveTopology(PrimitiveTopology.TRIANGLE_FAN)
        .build();

    public static final RenderPipeline GUI_TRIANGLE_STRIP = RenderPipeline.builder(GUI_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath("owo", "pipeline/gui_triangle_strip"))
        .withPrimitiveTopology(PrimitiveTopology.TRIANGLE_STRIP)
        .build();

    private static final RenderPipeline.Snippet GUI_TEXTURED_SNIPPET = RenderPipeline.builder(new RenderPipeline.Snippet[0])
        .withBindGroupLayout(BindGroupLayouts.GLOBALS)
        .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
        .withVertexShader("core/position_tex_color")
        .withFragmentShader("core/position_tex_color")
        .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
        .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
        .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
        .withPrimitiveTopology(PrimitiveTopology.QUADS)
        .buildSnippet();

    public static final RenderPipeline GUI_TEXTURED_NO_BLEND = RenderPipeline.builder(GUI_TEXTURED_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath("owo", "pipeline/gui_textured"))
        .withColorTargetState(ColorTargetState.DEFAULT)
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
