package io.wispforest.owo.ui.hud;

import io.wispforest.owo.ui.util.CommandOpenedScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
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
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);

        if (Hud.adapter == null) return;
        Hud.adapter.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void removed() {
        if (Hud.adapter != null) {
            Hud.suppress = false;
            Hud.adapter.enableInspector = false;
        }
    }
}
