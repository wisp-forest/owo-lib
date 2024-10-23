package io.wispforest.owo.mixin;

import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

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

    @Invoker("addDrawableChild")
    <T extends Element & Drawable & Selectable> T owo$addDrawableChild(T drawableElement);
}
