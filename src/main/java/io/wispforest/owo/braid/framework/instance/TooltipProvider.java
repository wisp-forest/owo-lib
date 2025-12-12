package io.wispforest.owo.braid.framework.instance;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface TooltipProvider {
    @Nullable List<ClientTooltipComponent> getTooltipComponentsAt(double x, double y);

    @Nullable
    default Style getStyleAt(double x, double y) {
        return null;
    }
}
