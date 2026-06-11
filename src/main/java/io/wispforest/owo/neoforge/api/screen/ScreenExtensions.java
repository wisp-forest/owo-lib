package io.wispforest.owo.neoforge.api.screen;

import net.minecraft.client.gui.screens.Screen;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Objects;
import java.util.WeakHashMap;

class ScreenExtensions {

    static WeakHashMap<Screen, ScreenExtensionsStorage> screenToEvents = new WeakHashMap<>();

    static ScreenExtensionsStorage getExtensions(Screen screen) {
        return Objects.requireNonNull(ScreenExtensions.screenToEvents.get(screen), "A given screen has yet to be setup for these events to be used!");
    }

    private static boolean isSetup = false;
    static void init() {
        if (isSetup) return;

        //--

        NeoForge.EVENT_BUS.<ScreenEvent.Init.Pre>addListener(init -> {
            var screen = init.getScreen();
            screenToEvents.computeIfAbsent(screen, _ -> new ScreenExtensionsStorage());
            ScreenEvents.BEFORE_INIT.invoker().beforeInit(screen.getMinecraft(), screen, screen.width, screen.height);
        });

        NeoForge.EVENT_BUS.<ScreenEvent.Init.Post>addListener(init -> {
            var screen = init.getScreen();
            ScreenEvents.AFTER_INIT.invoker().afterInit(screen.getMinecraft(), screen, screen.width, screen.height);
        });

        //--

        NeoForge.EVENT_BUS.<ScreenEvent.MouseButtonPressed.Pre>addListener(event -> {
            var screen = event.getScreen(); var mouseBtnEvent = event.getMouseButtonEvent();
            var result = getExtensions(event.getScreen()).fabric_getAllowMouseClickEvent()
                .invoker()
                .allowMouseClick(screen, mouseBtnEvent);
            if (!result) {
                event.setCanceled(true);
                return;
            }
            getExtensions(event.getScreen()).fabric_getBeforeMouseClickEvent()
                .invoker()
                .beforeMouseClick(screen, mouseBtnEvent);
        });

        NeoForge.EVENT_BUS.<ScreenEvent.MouseButtonReleased.Pre>addListener(event -> {
            var screen = event.getScreen(); var mouseBtnEvent = event.getMouseButtonEvent();
            var result = getExtensions(event.getScreen()).fabric_getAllowMouseReleaseEvent()
                .invoker()
                .allowMouseRelease(screen, mouseBtnEvent);
            if (!result) {
                event.setCanceled(true);
                return;
            }
            getExtensions(event.getScreen()).fabric_getBeforeMouseReleaseEvent()
                .invoker()
                .beforeMouseRelease(screen, mouseBtnEvent);
        });

        NeoForge.EVENT_BUS.<ScreenEvent.MouseDragged.Pre>addListener(event -> {
            var screen = event.getScreen(); var mouseBtnEvent = event.getMouseButtonEvent();
            var horizontalAmount = event.getDragX(); var verticalAmount = event.getDragY();
            var result = getExtensions(event.getScreen()).fabric_getAllowMouseDragEvent()
                .invoker()
                .allowMouseDrag(screen, mouseBtnEvent, horizontalAmount, verticalAmount);
            if (!result) {
                event.setCanceled(true);
                return;
            }
            getExtensions(event.getScreen()).fabric_getBeforeMouseDragEvent()
                .invoker()
                .beforeMouseDrag(screen, mouseBtnEvent, horizontalAmount, verticalAmount);
        });

        NeoForge.EVENT_BUS.<ScreenEvent.MouseScrolled.Pre>addListener(event -> {
            var screen = event.getScreen(); var mouseX = event.getMouseX(); var mouseY = event.getMouseY();
            var horizontalAmount = event.getScrollDeltaX(); var verticalAmount = event.getScrollDeltaY();
            var result = getExtensions(event.getScreen()).fabric_getAllowMouseScrollEvent()
                .invoker()
                .allowMouseScroll(screen, mouseX, mouseY, horizontalAmount, verticalAmount);
            if (!result) {
                event.setCanceled(true);
                return;
            }
            getExtensions(event.getScreen()).fabric_getBeforeMouseScrollEvent()
                .invoker()
                .beforeMouseScroll(screen, mouseX, mouseY, horizontalAmount, verticalAmount);
        });



        NeoForge.EVENT_BUS.<ScreenEvent.MouseButtonPressed.Post>addListener(event -> {
            var screen = event.getScreen(); var mouseBtnEvent = event.getMouseButtonEvent();
            getExtensions(event.getScreen()).fabric_getAfterMouseClickEvent()
                .invoker()
                .afterMouseClick(screen, mouseBtnEvent, event.wasClickHandled());
        });

        NeoForge.EVENT_BUS.<ScreenEvent.MouseButtonReleased.Post>addListener(event -> {
            var screen = event.getScreen(); var mouseBtnEvent = event.getMouseButtonEvent();
            getExtensions(event.getScreen()).fabric_getAfterMouseReleaseEvent()
                .invoker()
                .afterMouseRelease(screen, mouseBtnEvent, event.wasReleaseHandled());
        });

        // TODO: WILL NOT RUN IF ANY OF THE EVENT OF NEO OR MINECRAFT METHOD RETURNS THAT IT DID SOMETHING!
        NeoForge.EVENT_BUS.<ScreenEvent.MouseDragged.Post>addListener(event -> {
            var screen = event.getScreen(); var mouseBtnEvent = event.getMouseButtonEvent();
            var horizontalAmount = event.getDragX(); var verticalAmount = event.getDragY();
            getExtensions(event.getScreen()).fabric_getAfterMouseDragEvent()
                .invoker()
                .afterMouseDrag(screen, mouseBtnEvent, horizontalAmount, verticalAmount, false);
        });

        NeoForge.EVENT_BUS.<ScreenEvent.MouseScrolled.Post>addListener(event -> {
            var screen = event.getScreen(); var mouseX = event.getMouseX(); var mouseY = event.getMouseY();
            var horizontalAmount = event.getScrollDeltaX(); var verticalAmount = event.getScrollDeltaY();
            getExtensions(event.getScreen()).fabric_getAfterMouseScrollEvent()
                .invoker()
                .afterMouseScroll(screen, mouseX, mouseY, horizontalAmount, verticalAmount, false);
        });

        //--

        NeoForge.EVENT_BUS.<ScreenEvent.KeyPressed.Pre>addListener(event -> {
            var screen = event.getScreen(); var keyEvent = event.getKeyEvent();
            var result = getExtensions(event.getScreen()).fabric_getAllowKeyPressEvent()
                .invoker()
                .allowKeyPress(screen, keyEvent);
            if (!result) {
                event.setCanceled(true);
                return;
            }
            getExtensions(event.getScreen()).fabric_getBeforeKeyPressEvent()
                .invoker()
                .beforeKeyPress(screen, keyEvent);
        });

        NeoForge.EVENT_BUS.<ScreenEvent.KeyReleased.Pre>addListener(event -> {
            var screen = event.getScreen(); var keyEvent = event.getKeyEvent();
            var result = getExtensions(event.getScreen()).fabric_getAllowKeyReleaseEvent()
                .invoker()
                .allowKeyRelease(screen, keyEvent);
            if (!result) {
                event.setCanceled(true);
                return;
            }
            getExtensions(event.getScreen()).fabric_getBeforeKeyReleaseEvent()
                .invoker()
                .beforeKeyRelease(screen, keyEvent);
        });



        NeoForge.EVENT_BUS.<ScreenEvent.KeyPressed.Post>addListener(event -> {
            var screen = event.getScreen(); var keyEvent = event.getKeyEvent();
            getExtensions(event.getScreen()).fabric_getAfterKeyPressEvent()
                .invoker()
                .afterKeyPress(screen, keyEvent);
        });

        NeoForge.EVENT_BUS.<ScreenEvent.KeyReleased.Post>addListener(event -> {
            var screen = event.getScreen(); var keyEvent = event.getKeyEvent();
            getExtensions(event.getScreen()).fabric_getAfterKeyReleaseEvent()
                .invoker()
                .afterKeyRelease(screen, keyEvent);
        });

        //--

        isSetup = true;
    }
}
