package io.wispforest.owo.compat.emi;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.widget.Bounds;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.mixin.itemgroup.CreativeInventoryScreenAccessor;
import io.wispforest.owo.mixin.ui.access.BaseOwoHandledScreenAccessor;
import io.wispforest.owo.ui.base.BaseOwoHandledScreen;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.util.pond.OwoCreativeInventoryScreenExtensions;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;

import java.util.ArrayList;

public class OwoEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        registry.addExclusionArea(CreativeInventoryScreen.class, (screen, consumer) -> {
            var group = CreativeInventoryScreenAccessor.owo$getSelectedTab();
            if (!(group instanceof OwoItemGroup owoGroup)) return;
            if (owoGroup.getButtons().isEmpty()) return;

            int x = ((OwoCreativeInventoryScreenExtensions) screen).owo$getRootX();
            int y = ((OwoCreativeInventoryScreenExtensions) screen).owo$getRootY();

            int stackHeight = owoGroup.getButtonStackHeight();
            y -= 13 * (stackHeight - 4);

            for (int i = 0; i < owoGroup.getButtons().size(); i++) {
                int xOffset = x + 198 + (i / stackHeight) * 26;
                int yOffset = y + 10 + (i % stackHeight) * 30;
                consumer.accept(new Bounds(xOffset, yOffset, 24, 24));
            }
        });

        registry.addGenericExclusionArea((screen, consumer) -> {
            if (!(screen instanceof BaseOwoHandledScreen<?, ?> owoHandledScreen)) return;

            owoHandledScreen.getExclusionAreas()
                    .forEach(rect -> consumer.accept(new Bounds(rect.x(), rect.y(), rect.width(), rect.height())));
        });
    }
}
