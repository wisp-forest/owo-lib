package io.wispforest.owo.mixin.tweaks;

import com.mojang.blaze3d.Blaze3D;
import com.mojang.logging.LogUtils;
import net.minecraft.util.Util;
import org.lwjgl.sdl.SDLError;
import org.lwjgl.sdl.SDLMisc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import java.net.URI;

@Mixin(Blaze3D.class)
public abstract class OperatingSystemMixin {
    /**
     * @author glisco
     * @reason Keep URI opening asynchronous and avoid unconsumed subprocess
     * streams. Minecraft 26.3 delegates platform launching to SDL instead of
     * constructing xdg-open processes in Util.OS.
     */
    @Overwrite
    public static void openUri(URI uri) {
        Util.nonCriticalIoPool().execute(() -> {
            if (!SDLMisc.SDL_OpenURL(uri.toString())) {
                LogUtils.getLogger().error("Couldn't open uri '{}': {}", uri, SDLError.SDL_GetError());
            }
        });
    }
}
