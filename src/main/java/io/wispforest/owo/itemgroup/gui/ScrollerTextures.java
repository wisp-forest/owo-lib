package io.wispforest.owo.itemgroup.gui;

import net.minecraft.util.Identifier;

public record ScrollerTextures(Identifier enabled, Identifier disabled) {
    public Identifier getTexture(boolean isEnabled) {
        return isEnabled ? enabled : disabled;
    }
}
