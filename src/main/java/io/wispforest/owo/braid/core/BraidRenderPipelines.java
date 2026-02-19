package io.wispforest.owo.braid.core;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import io.wispforest.owo.Owo;
import net.minecraft.client.renderer.RenderPipelines;
import org.jetbrains.annotations.ApiStatus;

public class BraidRenderPipelines {
    public static final RenderPipeline TEXTURED_DEFAULT = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
        .withLocation(Owo.id("pipeline/braid_textured_default"))
        .build();

    public static final RenderPipeline TEXTURED_NEAREST = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
        .withLocation(Owo.id("pipeline/braid_textured_nearest"))
        .build();

    public static final RenderPipeline TEXTURED_BILINEAR = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
        .withLocation(Owo.id("pipeline/braid_textured_bilinear"))
        .build();

    @ApiStatus.Internal
    public static void register() {
        RenderPipelines.register(TEXTURED_DEFAULT);
        RenderPipelines.register(TEXTURED_NEAREST);
        RenderPipelines.register(TEXTURED_BILINEAR);
    }
}
