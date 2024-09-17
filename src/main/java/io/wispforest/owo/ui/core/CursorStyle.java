package io.wispforest.owo.ui.core;

import org.lwjgl.glfw.GLFW;

public enum CursorStyle {
    /**
     * The default cursor style defined by
     * the operating system
     */
    NONE(0),
    /**
     * The default arrow-style pointing cursor
     */
    POINTER(GLFW.GLFW_ARROW_CURSOR),

    /**
     * The text selection, usually I-beam, cursor
     */
    TEXT(GLFW.GLFW_IBEAM_CURSOR),

    /**
     * The hand cursor which signals clickable areas
     */
    HAND(GLFW.GLFW_HAND_CURSOR),

    /**
     * the Crosshair cursor
     */
    CROSSHAIR(GLFW.GLFW_CROSSHAIR_CURSOR),

    /**
     * The cross-shaped cursor which signals
     * draggable/movable areas
     */
    MOVE(GLFW.GLFW_RESIZE_ALL_CURSOR),

    /**
     * The horizontal resize cursor
     * @see #VERTICAL_RESIZE
     */
    HORIZONTAL_RESIZE(GLFW.GLFW_HRESIZE_CURSOR),

    /**
     * The vertical resize cursor
     * @see #HORIZONTAL_RESIZE
     */
    VERTICAL_RESIZE(GLFW.GLFW_VRESIZE_CURSOR),

    /**
     * The NorthWest-SouthEast resize cursor
     * @see #NESW_RESIZE
     *
     * @implNote This cursor style is not necessarily supported by all cursor themes
     */
    NWSE_RESIZE(GLFW.GLFW_RESIZE_NWSE_CURSOR),

    /**
     * The NorthEast-SouthWest resize cursor
     * @see #NWSE_RESIZE
     *
     * @implNote This cursor style is not necessarily supported by all cursor themes
     */
    NESW_RESIZE(GLFW.GLFW_RESIZE_NESW_CURSOR),


    /**
     * The Not-Allowed cursor style
     *
     * @implNote This cursor style is not necessarily supported by all cursor themes
     */
    NOT_ALLOWED(GLFW.GLFW_NOT_ALLOWED_CURSOR);


    public final int glfw;

    CursorStyle(int glfw) {this.glfw = glfw;}
}
