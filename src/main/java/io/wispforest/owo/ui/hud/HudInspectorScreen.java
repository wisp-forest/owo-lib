package io.wispforest.owo.ui.hud;

import io.wispforest.owo.ui.util.CommandOpenedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class HudInspectorScreen extends Screen implements CommandOpenedScreen {

    public HudInspectorScreen() {
        super(Text.empty());
        if (Hud.adapter != null) {
            Hud.suppress = true;
            Hud.adapter.enableInspector = true;
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);

        if (Hud.adapter == null) return;
        Hud.adapter.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void removed() {
        if (Hud.adapter != null) {
            Hud.suppress = false;
            Hud.adapter.enableInspector = false;
        }
    }
}
