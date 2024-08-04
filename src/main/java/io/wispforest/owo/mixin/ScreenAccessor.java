package io.wispforest.owo.mixin;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.PanoramaRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Screen.class)
public interface ScreenAccessor {
    @Accessor("PANORAMA_RENDERER")
    static CubeMap owo$PANORAMA_RENDERER() {
        throw new UnsupportedOperationException();
    }

    @Accessor("ROTATING_PANORAMA_RENDERER")
    static PanoramaRenderer owo$ROTATING_PANORAMA_RENDERER() {
        throw new UnsupportedOperationException();
    }
}
