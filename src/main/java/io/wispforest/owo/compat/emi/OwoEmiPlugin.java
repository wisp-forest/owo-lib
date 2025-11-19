package io.wispforest.owo.compat.emi;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.widget.Bounds;
import io.wispforest.owo.itemgroup.gui.OwoItemGroupRendererHandler;
import io.wispforest.owo.mixin.ui.layers.HandledScreenAccessor;
import io.wispforest.owo.ui.base.BaseOwoHandledScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;

public class OwoEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        registry.addExclusionArea(CreativeInventoryScreen.class, (screen, consumer) -> {
            int x = ((HandledScreenAccessor) screen).owo$getRootX();
            int y = ((HandledScreenAccessor) screen).owo$getRootY();

           OwoItemGroupRendererHandler.getSelectedRenderer()
                .getExclusionZones(x, y, rect -> new Bounds(rect.x(), rect.y(), rect.width(), rect.height()))
                .forEach(consumer);
        });

        registry.addGenericExclusionArea((screen, consumer) -> {
            if (!(screen instanceof BaseOwoHandledScreen<?, ?> owoHandledScreen)) return;

            owoHandledScreen
                .componentsForExclusionAreas(rect -> new Bounds(rect.x(), rect.y(), rect.width(), rect.height()))
                .forEach(consumer);
        });
    }
}
