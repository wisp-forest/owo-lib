package io.wispforest.owo.ui.renderstate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.gui.GuiRenderState;

public final class BlurQuadElementRenderState {

    public static void blurBackground(GuiRenderState renderState, float quality, float size) {
        var options = Minecraft.getInstance().options;
        var blurriness = Math.clamp(Math.round(quality * size / 5.0f), 0, 10);
        options.menuBackgroundBlurriness().set(blurriness);

        try {
            renderState.blurBeforeThisStratum();
        } catch (IllegalStateException ignored) {
        }
    }
}
