package io.wispforest.owo.compat.rei;

import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.util.OwoCreativeInventoryScreenExtensions;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.item.ItemGroup;

import java.util.ArrayList;
import java.util.Collections;

public class OwoReiPlugin implements REIClientPlugin {

    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        zones.register(CreativeInventoryScreen.class, screen -> {
            var group = ItemGroup.GROUPS[screen.getSelectedTab()];
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
    }
}
