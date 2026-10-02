package io.wispforest.owo.ui.core;

import org.lwjgl.sdl.SDLMouse;

public enum CursorStyle {
    /**
     * The default cursor style defined by
     * the operating system
     */
    NONE(0),
    /**
     * The default arrow-style pointing cursor
     */
    POINTER(SDLMouse.SDL_SYSTEM_CURSOR_DEFAULT),

    /**
     * The text selection, usually I-beam, cursor
     */
    TEXT(SDLMouse.SDL_SYSTEM_CURSOR_TEXT),

    /**
     * The hand cursor which signals clickable areas
     */
    HAND(SDLMouse.SDL_SYSTEM_CURSOR_POINTER),

    /**
     * the Crosshair cursor
     */
    CROSSHAIR(SDLMouse.SDL_SYSTEM_CURSOR_CROSSHAIR),

    /**
     * The cross-shaped cursor which signals
     * draggable/movable areas
     */
    MOVE(SDLMouse.SDL_SYSTEM_CURSOR_MOVE),

    /**
     * The horizontal resize cursor
     * @see #VERTICAL_RESIZE
     */
    HORIZONTAL_RESIZE(SDLMouse.SDL_SYSTEM_CURSOR_EW_RESIZE),

    /**
     * The vertical resize cursor
     * @see #HORIZONTAL_RESIZE
     */
    VERTICAL_RESIZE(SDLMouse.SDL_SYSTEM_CURSOR_NS_RESIZE),

    /**
     * The NorthWest-SouthEast resize cursor
     * @see #NESW_RESIZE
     *
     * @implNote This cursor style is not necessarily supported by all cursor themes
     */
    NWSE_RESIZE(SDLMouse.SDL_SYSTEM_CURSOR_NWSE_RESIZE),

    /**
     * The NorthEast-SouthWest resize cursor
     * @see #NWSE_RESIZE
     *
     * @implNote This cursor style is not necessarily supported by all cursor themes
     */
    NESW_RESIZE(SDLMouse.SDL_SYSTEM_CURSOR_NESW_RESIZE),


    /**
     * The Not-Allowed cursor style
     *
     * @implNote This cursor style is not necessarily supported by all cursor themes
     */
    NOT_ALLOWED(SDLMouse.SDL_SYSTEM_CURSOR_NOT_ALLOWED);


    public final int glfw;

    CursorStyle(int glfw) {this.glfw = glfw;}
}
