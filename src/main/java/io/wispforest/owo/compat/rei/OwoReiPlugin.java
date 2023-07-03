package io.wispforest.owo.compat.rei;

import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.mixin.itemgroup.CreativeInventoryScreenAccessor;
import io.wispforest.owo.mixin.ui.access.BaseOwoHandledScreenAccessor;
import io.wispforest.owo.ui.base.BaseOwoHandledScreen;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.util.pond.OwoCreativeInventoryScreenExtensions;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OwoReiPlugin implements REIClientPlugin {

    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        zones.register(CreativeInventoryScreen.class, screen -> {
            var group = CreativeInventoryScreenAccessor.owo$getSelectedTab();
            if (!(group instanceof OwoItemGroup owoGroup)) return Collections.emptySet();
            if (owoGroup.getButtons().isEmpty()) return Collections.emptySet();

            int x = ((OwoCreativeInventoryScreenExtensions) screen).owo$getRootX();
            int y = ((OwoCreativeInventoryScreenExtensions) screen).owo$getRootY();

            int stackHeight = owoGroup.getButtonStackHeight();
            y -= 13 * (stackHeight - 4);

            final var rectangles = new ArrayList<Rectangle>();
            for (int i = 0; i < owoGroup.getButtons().size(); i++) {
                int xOffset = x + 198 + (i / stackHeight) * 26;
                int yOffset = y + 10 + (i % stackHeight) * 30;
                rectangles.add(new Rectangle(xOffset, yOffset, 24, 24));
            }

            return rectangles;
        });

        zones.register(BaseOwoHandledScreen.class, screen -> {
            if (screen.children().isEmpty()) return List.of();

            var adapter = ((BaseOwoHandledScreenAccessor) screen).owo$getUIAdapter();
            if (adapter == null) return List.of();

            var rootComponent = adapter.rootComponent;
            var children = new ArrayList<Component>();
            rootComponent.collectChildren(children);
            children.remove(rootComponent);

            var rectangles = new ArrayList<Rectangle>();
            children.forEach(component -> {
                if (component instanceof ParentComponent parent && parent.surface() == Surface.BLANK) return;

                var size = component.fullSize();
                rectangles.add(new Rectangle(component.x(), component.y(), size.width(), size.height()));
            });
            return rectangles;
        });
    }
}
