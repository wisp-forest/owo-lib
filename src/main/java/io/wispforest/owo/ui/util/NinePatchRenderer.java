package io.wispforest.owo.ui.util;

import io.wispforest.owo.ui.core.Size;
import net.minecraft.util.Identifier;

/**
 * @deprecated Use the more appropriately named {@link NinePatchTexture} instead
 */
@Deprecated(forRemoval = true)
public class NinePatchRenderer extends NinePatchTexture {
    public NinePatchRenderer(Identifier texture, int u, int v, Size cornerPatchSize, Size centerPatchSize, Size textureSize, boolean repeat) {
        super(texture, u, v, cornerPatchSize, centerPatchSize, textureSize, repeat);
    }

    public NinePatchRenderer(Identifier texture, int u, int v, Size patchSize, Size textureSize, boolean repeat) {
        super(texture, u, v, patchSize, textureSize, repeat);
    }

    public NinePatchRenderer(Identifier texture, Size patchSize, Size textureSize, boolean repeat) {
        super(texture, patchSize, textureSize, repeat);
    }
}
