package io.wispforest.owo.ui.core;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.util.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.util.Util;

import java.util.function.Function;

public class OwoUIRenderLayers {

    public static final RenderLayer.MultiPhase GUI_TRIANGLE_FAN = RenderLayer.of(
        "owo:gui_triangle_fan_default_blend",
        0xc0000,
        OwoUIPipelines.GUI_TRIANGLE_FAN,
        RenderLayer.MultiPhaseParameters.builder().build(false)
    );

    public static final RenderLayer.MultiPhase GUI_TRIANGLE_STRIP = RenderLayer.of(
        "owo:gui_triangle_strip_default_blend",
        0xc0000,
        OwoUIPipelines.GUI_TRIANGLE_STRIP,
        RenderLayer.MultiPhaseParameters.builder().build(false)
    );

    public static final RenderLayer.MultiPhase GUI_SPECTRUM = RenderLayer.of(
        "owo:gui_spectrum",
        0xc0000,
        OwoUIPipelines.GUI_HSV,
        RenderLayer.MultiPhaseParameters.builder().build(false)
    );

    //--

    private static final Function<Identifier, RenderLayer> GUI_TEXTURED_NO_BLEND = Util.memoize(
        (texture) -> RenderLayer.of(
            "gui_textured",
            0xc0000,
            OwoUIPipelines.GUI_TEXTURED_NO_BLEND,
            RenderLayer.MultiPhaseParameters.builder()
                .texture(new RenderPhase.Texture(texture, TriState.FALSE, false))
                .build(false)
        )
    );

    public static RenderLayer getGuiTextured(Identifier texture, boolean blend) {
        return blend ? RenderLayer.getGuiTextured(texture) : GUI_TEXTURED_NO_BLEND.apply(texture);
    }

}
