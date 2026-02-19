package io.wispforest.owo.ui.hud;

import io.wispforest.owo.ui.util.CommandOpenedScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class HudInspectorScreen extends Screen implements CommandOpenedScreen {

    public HudInspectorScreen() {
        super(Component.empty());
        if (Hud.adapter != null) {
            Hud.suppress = true;
            Hud.adapter.enableInspector = true;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);

        if (Hud.adapter == null) return;
        Hud.adapter.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public void removed() {
        if (Hud.adapter != null) {
            Hud.suppress = false;
            Hud.adapter.enableInspector = false;
        }
    }
}
