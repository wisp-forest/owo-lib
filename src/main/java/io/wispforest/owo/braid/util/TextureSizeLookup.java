package io.wispforest.owo.braid.util;

import io.wispforest.owo.braid.core.Size;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class TextureSizeLookup {

    private static final Int2ObjectMap<Size> TEXTURE_SIZES = new Int2ObjectOpenHashMap<>();

    public static @Nullable Size sizeOf(Identifier texture) {
        return TEXTURE_SIZES.get(MinecraftClient.getInstance().getTextureManager().getTexture(texture).getGlId());
    }

    @ApiStatus.Internal
    public static void _registerTextureSize(int textureId, int width, int height) {
        TEXTURE_SIZES.put(textureId, Size.of(width, height));
    }
}