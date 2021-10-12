package com.glisco.owo.util;

import com.google.gson.Gson;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import static net.minecraft.client.gui.DrawableHelper.drawTexture;

public class AnimatedTextureDrawable implements Drawable {

    private final Identifier texture;
    private final SpriteSheetMetadata metadata;
    private final int validFrames;
    private final int delay;
    private final int width;
    private final int height;
    private final boolean loop;
    private final int rows;
    private int y;
    private int x;
    private long startTime = -1L;
    private float opacity = 1;

    /**
     * Creates a new animated texture widget that can be placed on your Screen or overlay etc.
     * @param x The x position of the widget.
     * @param y The y position of the widget.
     * @param texture The identifier of the textuer, eg: mymod:texture/animation_spritesheet.png
     * @param metadata Metadata on the spritesheet.
     * @param delay The delay, in milliseconds between each frame.
     */
    public AnimatedTextureDrawable(int x, int y, Identifier texture, SpriteSheetMetadata metadata, int delay, boolean loop) {
        this.x = x;
        this.y = y;
        this.texture = texture;
        this.delay = delay;
        this.metadata = metadata;
        this.loop = loop;

        FrameMetadata meta = metadata.frameMetadata();

        this.width = meta.width();
        this.height = meta.height();

        int columns = metadata.width() / meta.width();
        this.rows = metadata.height() / meta.height();
        this.validFrames = columns * this.rows;

        System.out.println(new Gson().toJson(this));
    }

    /**
     * Creates a new animated texture widget that can be placed on your Screen or overlay etc.
     * @param x The x position of the widget.
     * @param y The y position of the widget.
     * @param width The width of the widget.
     * @param height The height of the widget.
     * @param texture The identifier of the textuer, eg: mymod:texture/animation_spritesheet.png
     * @param metadata Metadata on the spritesheet.
     * @param delay The delay, in milliseconds between each frame.
     */
    public AnimatedTextureDrawable(int x, int y, int width, int height, Identifier texture, SpriteSheetMetadata metadata, int delay, boolean loop) {
        this.x = x;
        this.y = y;
        this.texture = texture;
        this.delay = delay;
        this.metadata = metadata;
        this.width = width;
        this.height = height;
        this.loop = loop;

        FrameMetadata meta = metadata.frameMetadata();

        int columns = metadata.width() / meta.width();
        this.rows = metadata.height() / meta.height();
        this.validFrames = columns * this.rows;
    }

    public void setOpacity(float opacity) {
        this.opacity = opacity;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if(startTime == -1L)
            startTime = Util.getMeasuringTimeMs();
        long currentTime = Util.getMeasuringTimeMs();
        long frame = Math.min(validFrames - 1, (currentTime - startTime) / delay);
        if(loop && frame == validFrames - 1) {
            startTime = Util.getMeasuringTimeMs();
            frame = 0;
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, opacity);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        drawTexture(matrices, x, y, (frame / rows) * metadata.frameMetadata().width(), (frame % rows) * metadata.frameMetadata().height(), width, height, metadata.width(), metadata.height());
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
    }
}
