package io.wispforest.owo.ui.core;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.client.OwoClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

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
}
