package io.wispforest.owo.itemgroup;

import io.wispforest.owo.client.texture.AnimatedTextureDrawable;
import io.wispforest.owo.client.texture.SpriteSheetMetadata;
import io.wispforest.owo.neoforge.env.EnvType;
import io.wispforest.owo.neoforge.env.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

/**
 * An icon used for rendering on buttons in {@link OwoItemGroup}s
 * <p>
 * Default implementations provided for textures and item stacks
 */
@FunctionalInterface
public interface Icon {

    @Environment(EnvType.CLIENT)
    void render(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY, float delta);

    static Icon of(Supplier<ItemStack> stack) {
        return new Icon() {

            @Nullable
            private ItemStack actualStack = null;

            @Override
            public void render(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY, float delta) {
                if (this.actualStack == null) {
                    this.actualStack = stack.get();
                }

                graphics.fakeItem(this.actualStack, x, y);
            }
        };
    }

    static Icon of(ItemLike item) {
        return of(() -> new ItemStack(item));
    }

    static Icon of(Identifier texture, int u, int v, int textureWidth, int textureHeight) {
        return new Icon() {
            @Override
            public void render(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY, float delta) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, 16, 16, textureWidth, textureHeight);
            }
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
        return new Icon() {
            @Override
            public void render(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY, float delta) {
                widget.render(x, y, graphics, mouseX, mouseY, delta);
            }
        };
    }
}
