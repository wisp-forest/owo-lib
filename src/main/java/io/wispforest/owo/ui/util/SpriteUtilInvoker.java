package io.wispforest.owo.ui.util;

import io.wispforest.owo.Owo;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.texture.Sprite;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class SpriteUtilInvoker {
    private static final MethodHandle MARK_SPRITE_ACTIVE = getMarkSpriteActive();

    public static void markSpriteActive(Sprite sprite) {
        try {
            MARK_SPRITE_ACTIVE.invoke((Sprite) sprite);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static MethodHandle getMarkSpriteActive() {
        if (FabricLoader.getInstance().isModLoaded("sodium")) {
            try {
                Class<?> spriteUtil = Class.forName("me.jellysquid.mods.sodium.client.render.texture.SpriteUtil");
                var m = spriteUtil.getMethod("markSpriteActive", Sprite.class);
                m.setAccessible(true);
                return MethodHandles.lookup().unreflect(m);
            } catch (Exception e) {
                Owo.LOGGER.error("Couldn't get SpriteUtil.markSpriteActive from Sodium", e);
            }
        }

        return MethodHandles.empty(MethodType.methodType(void.class, Sprite.class));
    }
}
