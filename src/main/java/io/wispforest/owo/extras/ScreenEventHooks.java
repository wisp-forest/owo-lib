package io.wispforest.owo.extras;

import net.minecraft.client.gui.screen.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

/**
 * Copy of the <a href="https://github.com/Sinytra/ForgifiedFabricAPI/blob/d82d50ba25d2bbeba29e5b09eeb935756c0adf8f/fabric-screen-api-v1/src/client/java/org/sinytra/fabric/screen_api/ScreenEventHooks.java">ScreenEventHooks</a>
 */
@EventBusSubscriber(value = Dist.CLIENT, modid = "owo")
public class ScreenEventHooks {

    @SubscribeEvent
    public static void beforeScreenDraw(ScreenEvent.Render.Pre event) {
        Screen screen = event.getScreen();
        ScreenEvents.beforeRender(screen).invoker().beforeRender(screen, event.getGuiGraphics(), event.getMouseX(), event.getMouseY(), event.getPartialTick());
    }

    @SubscribeEvent
    public static void afterScreenDraw(ScreenEvent.Render.Post event) {
        Screen screen = event.getScreen();
        ScreenEvents.afterRender(screen).invoker().afterRender(screen, event.getGuiGraphics(), event.getMouseX(), event.getMouseY(), event.getPartialTick());
    }

    @SubscribeEvent
    public static void beforeKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        Screen screen = event.getScreen();
        if (!ScreenEvents.allowKeyPress(screen).invoker().allowKeyPress(screen, event.getKeyCode(), event.getScanCode(), event.getModifiers())) {
            event.setCanceled(true);
        } else {
            ScreenEvents.beforeKeyPress(screen).invoker().beforeKeyPress(screen, event.getKeyCode(), event.getScanCode(), event.getModifiers());
        }
    }

    @SubscribeEvent
    public static void afterKeyPressed(ScreenEvent.KeyPressed.Post event) {
        Screen screen = event.getScreen();
        ScreenEvents.afterKeyPress(screen).invoker().afterKeyPress(screen, event.getKeyCode(), event.getScanCode(), event.getModifiers());
    }

    @SubscribeEvent
    public static void beforeKeyReleased(ScreenEvent.KeyReleased.Pre event) {
        Screen screen = event.getScreen();
        if (!ScreenEvents.allowKeyRelease(screen).invoker().allowKeyRelease(screen, event.getKeyCode(), event.getScanCode(), event.getModifiers())) {
            event.setCanceled(true);
        } else {
            ScreenEvents.beforeKeyRelease(screen).invoker().beforeKeyRelease(screen, event.getKeyCode(), event.getScanCode(), event.getModifiers());
        }
    }

    @SubscribeEvent
    public static void afterKeyReleased(ScreenEvent.KeyReleased.Post event) {
        Screen screen = event.getScreen();
        ScreenEvents.afterKeyRelease(screen).invoker().afterKeyRelease(screen, event.getKeyCode(), event.getScanCode(), event.getModifiers());
    }

    @SubscribeEvent
    public static void beforeMouseClicked(ScreenEvent.MouseButtonPressed.Pre event) {
        Screen screen = event.getScreen();
        if (!ScreenEvents.allowMouseClick(screen).invoker().allowMouseClick(screen, event.getMouseX(), event.getMouseY(), event.getButton())) {
            event.setCanceled(true);
        } else {
            ScreenEvents.beforeMouseClick(screen).invoker().beforeMouseClick(screen, event.getMouseX(), event.getMouseY(), event.getButton());
        }
    }

    @SubscribeEvent
    public static void afterMouseClicked(ScreenEvent.MouseButtonPressed.Post event) {
        Screen screen = event.getScreen();
        ScreenEvents.afterMouseClick(screen).invoker().afterMouseClick(screen, event.getMouseX(), event.getMouseY(), event.getButton());
    }

    @SubscribeEvent
    public static void beforeMouseReleased(ScreenEvent.MouseButtonReleased.Pre event) {
        Screen screen = event.getScreen();
        if (!ScreenEvents.allowMouseRelease(screen).invoker().allowMouseRelease(screen, event.getMouseX(), event.getMouseY(), event.getButton())) {
            event.setCanceled(true);
        } else {
            ScreenEvents.beforeMouseRelease(screen).invoker().beforeMouseRelease(screen, event.getMouseX(), event.getMouseY(), event.getButton());
        }
    }

    @SubscribeEvent
    public static void afterMouseReleased(ScreenEvent.MouseButtonReleased.Post event) {
        Screen screen = event.getScreen();
        ScreenEvents.afterMouseRelease(screen).invoker().afterMouseRelease(screen, event.getMouseX(), event.getMouseY(), event.getButton());
    }

    @SubscribeEvent
    public static void beforeMouseScroll(ScreenEvent.MouseScrolled.Pre event) {
        Screen screen = event.getScreen();
        if (!ScreenEvents.allowMouseScroll(screen).invoker().allowMouseScroll(screen, event.getMouseX(), event.getMouseY(), event.getScrollDeltaX(), event.getScrollDeltaY())) {
            event.setCanceled(true);
        } else {
            ScreenEvents.beforeMouseScroll(screen).invoker().beforeMouseScroll(screen, event.getMouseX(), event.getMouseY(), event.getScrollDeltaX(), event.getScrollDeltaY());
        }
    }

    @SubscribeEvent
    public static void afterMouseScroll(ScreenEvent.MouseScrolled.Post event) {
        Screen screen = event.getScreen();
        ScreenEvents.afterMouseScroll(screen).invoker().afterMouseScroll(screen, event.getMouseX(), event.getMouseY(), event.getScrollDeltaX(), event.getScrollDeltaY());
    }
}
