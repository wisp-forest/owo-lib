package io.wispforest.owo.client.texture;


/**
 * A simple container to define the sprite sheet an {@link AnimatedTextureDrawable} uses
 *
 * <p>Originally from Animawid, adapted for oωo</p>
 *
 * @author Tempora
 * @author glisco
 */
public record SpriteSheetMetadata(int width, int height, int frameWidth, int frameHeight, int offset) {

    /**
     * Creates a new SpriteSheetMetadata object.
     *
     * @param width       The width of the Sprite Sheet.
     * @param height      The height of the Sprite Sheet.
     * @param frameWidth  The width of each individual frame
     * @param frameHeight The width of each individual frame
     */
    public SpriteSheetMetadata(int width, int height, int frameWidth, int frameHeight) {
        this(width, height, frameWidth, frameHeight, 0);
    }

    /**
     * Convenience constructor that assumes both the spritesheet and frames are square
     */
    public SpriteSheetMetadata(int size, int frameSize) {
        this(size, size, frameSize, frameSize, 0);
    }
}
