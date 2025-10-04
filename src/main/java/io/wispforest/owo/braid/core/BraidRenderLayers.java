package io.wispforest.owo.braid.core;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.util.Util;

import java.util.function.BiFunction;

import static net.minecraft.client.render.RenderPhase.*;

public final class BraidRenderLayers {

    private BraidRenderLayers() {}

    public static RenderLayer getTextured(Identifier texture, TriState bilinear) {
        return TEXTURED.apply(texture, bilinear);
    }

    private static final BiFunction<Identifier, TriState, RenderLayer> TEXTURED = Util.memoize(
        (texture, bilinear) -> RenderLayer.of(
            "gui_textured",
            VertexFormats.POSITION_TEXTURE_COLOR,
            VertexFormat.DrawMode.QUADS,
            786432,
            RenderLayer.MultiPhaseParameters.builder()
                .texture(new Texture(texture, bilinear, false))
                .program(POSITION_TEXTURE_COLOR_PROGRAM)
                .transparency(TRANSLUCENT_TRANSPARENCY)
                .depthTest(LEQUAL_DEPTH_TEST)
                .build(false)
        )
    );
}
