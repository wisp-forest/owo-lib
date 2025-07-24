package io.wispforest.owo.braid.framework.instance;

import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.Style;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface TooltipProvider {
    @Nullable List<TooltipComponent> getTooltipComponentsAt(double x, double y);

    @Nullable
    default Style getStyleAt(double x, double y) {
        return null;
    }
}
