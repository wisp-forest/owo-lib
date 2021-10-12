package com.glisco.owo.util;


public record SpriteSheetMetadata(int width, int height, int offset, FrameMetadata frameMetadata) {
    /**
     * Creates a new SpriteSheetMetadata object.
     * @param width The width of the Sprite Sheet.
     * @param height The height of the Sprite Sheet.
     * @param frameMetadata The individual frame metadata.
     */
    public SpriteSheetMetadata(int width, int height, FrameMetadata frameMetadata) {
        this(width, height, 0, frameMetadata);
    }
}
