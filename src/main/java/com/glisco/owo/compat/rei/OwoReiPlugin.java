package com.glisco.owo.compat.rei;

import com.glisco.owo.itemgroup.OwoItemGroup;
import com.glisco.owo.util.ScreenRootSupplier;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.item.ItemGroup;

import java.util.Collections;

public class OwoReiPlugin implements REIClientPlugin {

    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        zones.register(CreativeInventoryScreen.class, screen -> {
            var group = ItemGroup.GROUPS[screen.getSelectedTab()];
            if (!(group instanceof OwoItemGroup owoGroup)) return Collections.emptySet();
            if (owoGroup.getButtons().isEmpty()) return Collections.emptySet();

            int x = ((ScreenRootSupplier)screen).getRootX();
            int y = ((ScreenRootSupplier) screen).getRootY();

            return Collections.singleton(new Rectangle(x + 200, y + 15, 28, 25 * owoGroup.getButtons().size()));
        });
    }
}
