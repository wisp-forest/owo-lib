package io.wispforest.owo.neoforge.api.screen;

import net.fabricmc.fabric.api.event.Event;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;

import java.util.Objects;

public class ScreenKeyboardEvents {
    static {
        ScreenExtensions.init();
    }

    /**
     * An event that checks if a key press should be allowed.
     *
     * @return the event
     */
    public static Event<AllowKeyPress> allowKeyPress(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).fabric_getAllowKeyPressEvent();
    }

    /**
     * An event that is called before a key press is processed for a screen.
     *
     * @return the event
     */
    public static Event<BeforeKeyPress> beforeKeyPress(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).fabric_getBeforeKeyPressEvent();
    }

    /**
     * An event that is called after a key press is processed for a screen.
     *
     * @return the event
     */
    public static Event<AfterKeyPress> afterKeyPress(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).fabric_getAfterKeyPressEvent();
    }

    /**
     * An event that checks if a pressed key should be allowed to release.
     *
     * @return the event
     */
    public static Event<AllowKeyRelease> allowKeyRelease(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).fabric_getAllowKeyReleaseEvent();
    }

    /**
     * An event that is called after the release of a key is processed for a screen.
     *
     * @return the event
     */
    public static Event<BeforeKeyRelease> beforeKeyRelease(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).fabric_getBeforeKeyReleaseEvent();
    }

    /**
     * An event that is called after the release a key is processed for a screen.
     *
     * @return the event
     */
    public static Event<AfterKeyRelease> afterKeyRelease(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).fabric_getAfterKeyReleaseEvent();
    }

    private ScreenKeyboardEvents() {
    }

    @FunctionalInterface
    public interface AllowKeyPress {
        /**
         * Checks if a key should be allowed to be pressed.
         *
         * @param event the key press event, containing the key, scancode and modifiers
         * @return whether the key press should be processed
         * @see org.lwjgl.glfw.GLFW#GLFW_KEY_Q
         * @see <a href="https://www.glfw.org/docs/3.3/group__mods.html">Modifier key flags</a>
         */
        boolean allowKeyPress(Screen screen, KeyEvent event);
    }

    @FunctionalInterface
    public interface BeforeKeyPress {
        /**
         * Called before a key press is handled.
         *
         * @param event the key press event, containing the key, scancode and modifiers
         * @see org.lwjgl.glfw.GLFW#GLFW_KEY_Q
         * @see <a href="https://www.glfw.org/docs/3.3/group__mods.html">Modifier key flags</a>
         */
        void beforeKeyPress(Screen screen, KeyEvent event);
    }

    @FunctionalInterface
    public interface AfterKeyPress {
        /**
         * Called after a key press is handled.
         *
         * @param event the key press event, containing the key, scancode and modifiers
         * @see org.lwjgl.glfw.GLFW#GLFW_KEY_Q
         * @see <a href="https://www.glfw.org/docs/3.3/group__mods.html">Modifier key flags</a>
         */
        void afterKeyPress(Screen screen, KeyEvent event);
    }

    @FunctionalInterface
    public interface AllowKeyRelease {
        /**
         * Checks if a pressed key should be allowed to be released.
         *
         * @param event the key press event, containing the key, scancode and modifiers
         * @return whether the key press should be released
         * @see org.lwjgl.glfw.GLFW#GLFW_KEY_Q
         * @see <a href="https://www.glfw.org/docs/3.3/group__mods.html">Modifier key flags</a>
         */
        boolean allowKeyRelease(Screen screen, KeyEvent event);
    }

    @FunctionalInterface
    public interface BeforeKeyRelease {
        /**
         * Called before a pressed key has been released.
         *
         * @param event the key press event, containing the key, scancode and modifiers
         * @see org.lwjgl.glfw.GLFW#GLFW_KEY_Q
         * @see <a href="https://www.glfw.org/docs/3.3/group__mods.html">Modifier key flags</a>
         */
        void beforeKeyRelease(Screen screen, KeyEvent event);
    }

    @FunctionalInterface
    public interface AfterKeyRelease {
        /**
         * Called after a pressed key has been released.
         *
         * @param event the key press event, containing the key, scancode and modifiers
         * @see org.lwjgl.glfw.GLFW#GLFW_KEY_Q
         * @see <a href="https://www.glfw.org/docs/3.3/group__mods.html">Modifier key flags</a>
         */
        void afterKeyRelease(Screen screen, KeyEvent event);
    }
}
