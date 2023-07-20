package io.wispforest.owo.itemgroup;

import io.wispforest.owo.client.texture.AnimatedTextureDrawable;
import io.wispforest.owo.client.texture.SpriteSheetMetadata;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/**
 * An icon used for rendering on buttons in {@link OwoItemGroup}s
 * <p>
 * Default implementations provided for textures and item stacks
 */
@FunctionalInterface
public interface Icon {

    void render(DrawContext context, int x, int y, int mouseX, int mouseY, float delta);

    static Icon of(ItemStack stack) {
        return (context, x, y, mouseX, mouseY, delta) -> {
            context.drawItemWithoutEntity(stack, x, y);
        };
    }

    static Icon of(ItemConvertible item) {
        return of(new ItemStack(item));
    }

    static Icon of(Identifier texture, int u, int v, int textureWidth, int textureHeight) {
        return (context, x, y, mouseX, mouseY, delta) -> {
            context.drawTexture(texture, x, y, u, v, 16, 16, textureWidth, textureHeight);
        };
    }

    /**
     * Creates an Animated ItemGroup Icon
     *
     * @param texture     The texture to render, this is the spritesheet
     * @param textureSize The size of the texture, it is assumed to be square
     * @param frameDelay  The delay in milliseconds between frames.
     * @param loop        Should the animation play once or loop?
     * @return The created icon instance
     */
    static Icon of(Identifier texture, int textureSize, int frameDelay, boolean loop) {
        var widget = new AnimatedTextureDrawable(0, 0, 16, 16, texture, new SpriteSheetMetadata(textureSize, 16), frameDelay, loop);
        return (context, x, y, mouseX, mouseY, delta) -> {
            widget.render(x, y, context, mouseX, mouseY, delta);
        };
    }
}
