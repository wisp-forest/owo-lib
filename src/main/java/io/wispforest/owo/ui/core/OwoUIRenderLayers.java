package io.wispforest.owo.ui.core;

import io.wispforest.owo.client.OwoClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.util.Util;

import java.util.function.BiFunction;

import static net.minecraft.client.render.RenderPhase.*;

public class OwoUIRenderLayers {

    public static final RenderLayer.MultiPhase GUI_TRIANGLE_FAN = RenderLayer.of(
            "owo:gui_triangle_fan_default_blend",
            VertexFormats.POSITION_COLOR,
            VertexFormat.DrawMode.TRIANGLE_FAN,
            786432,
            RenderLayer.MultiPhaseParameters.builder().program(GUI_PROGRAM).transparency(TRANSLUCENT_TRANSPARENCY).depthTest(LEQUAL_DEPTH_TEST).build(false)
    );

    public static final RenderLayer.MultiPhase GUI_TRIANGLE_STRIP = RenderLayer.of(
            "owo:gui_triangle_strip_default_blend",
            VertexFormats.POSITION_COLOR,
            VertexFormat.DrawMode.TRIANGLE_FAN,
            786432,
            RenderLayer.MultiPhaseParameters.builder().program(GUI_PROGRAM).transparency(TRANSLUCENT_TRANSPARENCY).depthTest(LEQUAL_DEPTH_TEST).build(false)
    );

    public static final RenderLayer.MultiPhase GUI_SPECTRUM = RenderLayer.of(
            "owo:gui_spectrum",
            VertexFormats.POSITION_COLOR,
            VertexFormat.DrawMode.QUADS,
            786432,
            RenderLayer.MultiPhaseParameters.builder().program(OwoClient.HSV_PROGRAM.renderPhaseProgram()).depthTest(LEQUAL_DEPTH_TEST).build(false)
    );

    public static final RenderLayer.MultiPhase GUI_NO_DEPTH = RenderLayer.of(
            "owo:gui_no_depth",
            VertexFormats.POSITION_COLOR,
            VertexFormat.DrawMode.QUADS,
            786432,
            RenderLayer.MultiPhaseParameters.builder().program(GUI_PROGRAM).transparency(TRANSLUCENT_TRANSPARENCY).build(false)
    );

    //--

    private static final BiFunction<Identifier, Boolean, RenderLayer> GUI_TEXTURED = Util.memoize(
            (texture, bl) -> RenderLayer.of(
                    "gui_textured",
                    VertexFormats.POSITION_TEXTURE_COLOR,
                    VertexFormat.DrawMode.QUADS,
                    786432,
                    RenderLayer.MultiPhaseParameters.builder()
                            .texture(new RenderPhase.Texture(texture, TriState.FALSE, false))
                            .program(POSITION_TEXTURE_COLOR_PROGRAM)
                            .transparency(bl ? TRANSLUCENT_TRANSPARENCY : NO_TRANSPARENCY)
                            .depthTest(LEQUAL_DEPTH_TEST)
                            .build(false)
            )
    );

    public static RenderLayer getGuiTextured(Identifier texture, boolean blend) {
        return GUI_TEXTURED.apply(texture, blend);
    }

}
