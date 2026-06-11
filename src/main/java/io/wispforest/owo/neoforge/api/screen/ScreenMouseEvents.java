package io.wispforest.owo.neoforge.api.screen;

import net.fabricmc.fabric.api.event.Event;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.Objects;

public final class ScreenMouseEvents {
    static {
        ScreenExtensions.init();
    }

    public static Event<AllowMouseClick> allowMouseClick(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).fabric_getAllowMouseClickEvent();
    }

    public static Event<BeforeMouseClick> beforeMouseClick(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).fabric_getBeforeMouseClickEvent();
    }

    public static Event<AfterMouseClick> afterMouseClick(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).fabric_getAfterMouseClickEvent();
    }

    public static Event<AllowMouseRelease> allowMouseRelease(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).fabric_getAllowMouseReleaseEvent();
    }

    public static Event<BeforeMouseRelease> beforeMouseRelease(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).fabric_getBeforeMouseReleaseEvent();
    }

    public static Event<AfterMouseRelease> afterMouseRelease(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).fabric_getAfterMouseReleaseEvent();
    }

    public static Event<AllowMouseDrag> allowMouseDrag(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).fabric_getAllowMouseDragEvent();
    }

    public static Event<BeforeMouseDrag> beforeMouseDrag(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).fabric_getBeforeMouseDragEvent();
    }

    public static Event<AfterMouseDrag> afterMouseDrag(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).fabric_getAfterMouseDragEvent();
    }

    public static Event<AllowMouseScroll> allowMouseScroll(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).fabric_getAllowMouseScrollEvent();
    }

    public static Event<BeforeMouseScroll> beforeMouseScroll(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).fabric_getBeforeMouseScrollEvent();
    }

    public static Event<AfterMouseScroll> afterMouseScroll(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).fabric_getAfterMouseScrollEvent();
    }

    private ScreenMouseEvents() {
    }

    @FunctionalInterface
    public interface AllowMouseClick {
        /**
         * Checks if the mouse click should be allowed in a screen.
         *
         * @param screen the screen
         * @param event the mouse button event, containing the mouse position and button
         * @see org.lwjgl.glfw.GLFW#GLFW_MOUSE_BUTTON_1
         */
        boolean allowMouseClick(Screen screen, MouseButtonEvent event);
    }

    @FunctionalInterface
    public interface BeforeMouseClick {
        /**
         * Called before a mouse click in a screen.
         *
         * @param screen the screen
         * @param event the mouse button event, containing the mouse position and button
         * @see org.lwjgl.glfw.GLFW#GLFW_MOUSE_BUTTON_1
         */
        void beforeMouseClick(Screen screen, MouseButtonEvent event);
    }

    @FunctionalInterface
    public interface AfterMouseClick {
        /**
         * Called after a mouse click in a screen.
         *
         * @param screen the screen
         * @param event the mouse button event, containing the mouse position and button
         * @param consumed whether the mouse click was already consumed
         * @see org.lwjgl.glfw.GLFW#GLFW_MOUSE_BUTTON_1
         */
        boolean afterMouseClick(Screen screen, MouseButtonEvent event, boolean consumed);
    }

    @FunctionalInterface
    public interface AllowMouseRelease {
        /**
         * Checks if the mouse click should be allowed to release in a screen.
         *
         * @param screen the screen
         * @param event the mouse button event, containing the mouse position and button
         * @see org.lwjgl.glfw.GLFW#GLFW_MOUSE_BUTTON_1
         */
        boolean allowMouseRelease(Screen screen, MouseButtonEvent event);
    }

    @FunctionalInterface
    public interface BeforeMouseRelease {
        /**
         * Called before a mouse click has released in a screen.
         *
         * @param screen the screen
         * @param event the mouse button event, containing the mouse position and button
         * @see org.lwjgl.glfw.GLFW#GLFW_MOUSE_BUTTON_1
         */
        void beforeMouseRelease(Screen screen, MouseButtonEvent event);
    }

    @FunctionalInterface
    public interface AfterMouseRelease {
        /**
         * Called after a mouse click has released in a screen.
         *
         * @param screen the screen
         * @param event the mouse release event, containing the mouse position and button
         * @param consumed whether the mouse release was already consumed
         * @see org.lwjgl.glfw.GLFW#GLFW_MOUSE_BUTTON_1
         */
        boolean afterMouseRelease(Screen screen, MouseButtonEvent event, boolean consumed);
    }

    @FunctionalInterface
    public interface AllowMouseDrag {
        /**
         * Checks if the mouse should be allowed to drag in a screen by moving the cursor while a mouse button is held
         * down.
         *
         * @param screen the screen
         * @param event the mouse button event, containing the mouse position and button
         * @param horizontalAmount the horizontal drag amount
         * @param verticalAmount the vertical drag amount
         * @return whether the mouse should be allowed to drag
         * @see org.lwjgl.glfw.GLFW#GLFW_MOUSE_BUTTON_1
         */
        boolean allowMouseDrag(Screen screen, MouseButtonEvent event, double horizontalAmount, double verticalAmount);
    }

    @FunctionalInterface
    public interface BeforeMouseDrag {
        /**
         * Called before a mouse is dragged on screen.
         *
         * @param screen the screen
         * @param event the mouse button event, containing the mouse position and button
         * @param horizontalAmount the horizontal drag amount
         * @param verticalAmount the vertical drag amount
         * @see org.lwjgl.glfw.GLFW#GLFW_MOUSE_BUTTON_1
         */
        void beforeMouseDrag(Screen screen, MouseButtonEvent event, double horizontalAmount, double verticalAmount);
    }

    @FunctionalInterface
    public interface AfterMouseDrag {
        /**
         * Called after a mouse is dragged on screen.
         *
         * @param screen the screen
         * @param event the mouse button event, containing the mouse position and button
         * @param horizontalAmount the horizontal drag amount
         * @param verticalAmount the vertical drag amount
         * @param consumed whether the mouse drag was already consumed
         * @see org.lwjgl.glfw.GLFW#GLFW_MOUSE_BUTTON_1
         */
        boolean afterMouseDrag(Screen screen, MouseButtonEvent event, double horizontalAmount, double verticalAmount, boolean consumed);
    }

    @FunctionalInterface
    public interface AllowMouseScroll {
        /**
         * Checks if the mouse should be allowed to scroll in a screen.
         *
         * @param screen the screen
         * @param mouseX the x position of the mouse
         * @param mouseY the y position of the mouse
         * @param horizontalAmount the horizontal scroll amount
         * @param verticalAmount the vertical scroll amount
         * @return whether the mouse should be allowed to scroll
         */
        boolean allowMouseScroll(Screen screen, double mouseX, double mouseY, double horizontalAmount, double verticalAmount);
    }

    @FunctionalInterface
    public interface BeforeMouseScroll {
        /**
         * Called before a mouse has scrolled on screen.
         *
         * @param screen the screen
         * @param mouseX the x position of the mouse
         * @param mouseY the y position of the mouse
         * @param horizontalAmount the horizontal scroll amount
         * @param verticalAmount the vertical scroll amount
         */
        void beforeMouseScroll(Screen screen, double mouseX, double mouseY, double horizontalAmount, double verticalAmount);
    }

    @FunctionalInterface
    public interface AfterMouseScroll {
        /**
         * Called after a mouse has scrolled on screen.
         *
         * @param screen the screen
         * @param mouseX the x position of the mouse
         * @param mouseY the y position of the mouse
         * @param horizontalAmount the horizontal scroll amount
         * @param verticalAmount the vertical scroll amount
         * @param consumed whether the mouse scroll was already consumed
         */
        boolean afterMouseScroll(Screen screen, double mouseX, double mouseY, double horizontalAmount, double verticalAmount, boolean consumed);
    }
}
