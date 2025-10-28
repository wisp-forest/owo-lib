package io.wispforest.owo.mixin;

import net.minecraft.client.gui.CubeMapRenderer;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Screen.class)
public interface ScreenAccessor {
    @Accessor("PANORAMA_RENDERER")
    static CubeMapRenderer owo$PANORAMA_RENDERER() {
        throw new UnsupportedOperationException();
    }

    @Accessor("ROTATING_PANORAMA_RENDERER")
    static RotatingCubeMapRenderer owo$ROTATING_PANORAMA_RENDERER() {
        throw new UnsupportedOperationException();
    }
}
