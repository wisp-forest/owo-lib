package io.wispforest.owo.compat.emi;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.widget.Bounds;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.mixin.itemgroup.CreativeInventoryScreenAccessor;
import io.wispforest.owo.ui.base.BaseOwoHandledScreen;
import io.wispforest.owo.util.pond.OwoCreativeInventoryScreenExtensions;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;

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

            owoHandledScreen.componentsForExclusionAreas()
                .map(component -> new Bounds(component.x(), component.y(), component.width(), component.height()))
                .forEach(consumer);
        });
    }
}
