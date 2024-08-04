package io.wispforest.owo.compat.emi;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.widget.Bounds;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.mixin.itemgroup.CreativeInventoryScreenAccessor;
import io.wispforest.owo.mixin.ui.access.BaseOwoHandledScreenAccessor;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.core.Size;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.util.pond.OwoCreativeInventoryScreenExtensions;
import java.util.ArrayList;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;

public class OwoEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        registry.addExclusionArea(CreativeModeInventoryScreen.class, (screen, consumer) -> {
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
            if (screen.children().isEmpty() || !(screen instanceof BaseOwoHandledScreenAccessor accessor)) return;

            var adapter = accessor.owo$getUIAdapter();
            if (adapter == null) return;

            var rootComponent = adapter.rootComponent;
            var children = new ArrayList<Component>();
            rootComponent.collectDescendants(children);
            children.remove(rootComponent);

            children.forEach(component -> {
                if (component instanceof ParentComponent parent && parent.surface() == Surface.BLANK) return;

                var size = component.fullSize();
                consumer.accept(new Bounds(component.x(), component.y(), size.width(), size.height()));
            });
        });
    }
}
