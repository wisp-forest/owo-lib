package io.wispforest.owo.ui.renderstate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.gui.GuiRenderState;

public final class BlurQuadElementRenderState {

    public static void blurBackground(GuiRenderState renderState, int quality, float size) {
        var options = Minecraft.getInstance().options;
        var blurriness = Math.clamp(Math.round(quality * 2L), 0, 10);
        options.menuBackgroundBlurriness().set(blurriness);

        try {
            renderState.blurBeforeThisStratum();
        } catch (IllegalStateException ignored) {
        }
    }
}
