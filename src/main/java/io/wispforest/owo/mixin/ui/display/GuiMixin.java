package io.wispforest.owo.mixin.ui.display;

import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Gui.class)
public class GuiMixin {
    // FIXME 26.2: extractCrosshair + CROSSHAIR_SPRITE removed from Gui.
    // Braid display cursor crosshair customization needs new approach.
}
