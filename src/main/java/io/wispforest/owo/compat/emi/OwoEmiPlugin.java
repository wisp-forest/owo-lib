package io.wispforest.owo.compat.emi;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.widget.Bounds;
import io.wispforest.owo.itemgroup.base.OwoItemGroupState;
import io.wispforest.owo.mixin.itemgroup.CreativeInventoryScreenAccessor;
import io.wispforest.owo.ui.base.BaseOwoHandledScreen;
import io.wispforest.owo.util.pond.OwoCreativeInventoryScreenExtensions;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;

public class OwoEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        registry.addExclusionArea(CreativeInventoryScreen.class, (screen, consumer) -> {
            var state = OwoItemGroupState.get(CreativeInventoryScreenAccessor.owo$getSelectedTab());
            if (state == null) return;

            int x = ((OwoCreativeInventoryScreenExtensions) screen).owo$getRootX();
            int y = ((OwoCreativeInventoryScreenExtensions) screen).owo$getRootY();

            state.getExclusionZones(x, y).forEach(rect -> consumer.accept(new Bounds(rect.position().x(), rect.position().y(), rect.width(), rect.height())));
        });

        registry.addGenericExclusionArea((screen, consumer) -> {
            if (!(screen instanceof BaseOwoHandledScreen<?, ?> owoHandledScreen)) return;

            owoHandledScreen.componentsForExclusionAreas()
                .map(component -> new Bounds(component.x(), component.y(), component.width(), component.height()))
                .forEach(consumer);
        });
    }
}
