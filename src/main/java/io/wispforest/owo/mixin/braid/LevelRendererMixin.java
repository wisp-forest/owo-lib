package io.wispforest.owo.mixin.braid;

import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    // FIXME 26.2: SubmitNodeStorage field + lambda target changed.
    // Braid display rendering needs new injection point.
}
