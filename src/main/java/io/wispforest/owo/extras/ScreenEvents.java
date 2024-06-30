package io.wispforest.owo.extras;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;

import java.util.List;
import java.util.Objects;

/**
 * Copy of Fabric Events:
 * <pre>
 * - <a href="https://github.com/FabricMC/fabric/blob/e541ce876ccb928afd1c777fbbe9b53851df431f/fabric-screen-api-v1/src/client/java/net/fabricmc/fabric/api/client/screen/v1/ScreenEvents.java">ScreenEvents</a>
 * - <a href="https://github.com/FabricMC/fabric/blob/e541ce876ccb928afd1c777fbbe9b53851df431f/fabric-screen-api-v1/src/client/java/net/fabricmc/fabric/api/client/screen/v1/ScreenKeyboardEvents.java">ScreenKeyboardEvents</a>
 * - <a href="https://github.com/FabricMC/fabric/blob/e541ce876ccb928afd1c777fbbe9b53851df431f/fabric-screen-api-v1/src/client/java/net/fabricmc/fabric/api/client/screen/v1/ScreenMouseEvents.java">ScreenMouseEvents</a>
 * </pre>
 */
public class ScreenEvents {
    private ScreenEvents() {}

    public static final Event<BeforeInit> BEFORE_INIT = EventFactory.createArrayBacked(BeforeInit.class, callbacks -> (client, screen, scaledWidth, scaledHeight) -> {
        for (BeforeInit callback : callbacks) {
            callback.beforeInit(client, screen, scaledWidth, scaledHeight);
        }
    });

    public static final Event<AfterInit> AFTER_INIT = EventFactory.createArrayBacked(AfterInit.class, callbacks -> (client, screen, scaledWidth, scaledHeight) -> {
        for (AfterInit callback : callbacks) {
            callback.afterInit(client, screen, scaledWidth, scaledHeight);
        }
    });

    public static Event<Remove> remove(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).owo_getRemoveEvent();
    }

    public static Event<BeforeRender> beforeRender(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).owo_getBeforeRenderEvent();
    }

    public static Event<AfterRender> afterRender(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).owo_getAfterRenderEvent();
    }

    public static Event<BeforeTick> beforeTick(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).owo_getBeforeTickEvent();
    }

    public static Event<AfterTick> afterTick(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).owo_getAfterTickEvent();
    }

    @FunctionalInterface
    public interface BeforeInit {
        void beforeInit(MinecraftClient client, Screen screen, int scaledWidth, int scaledHeight);
    }

    @FunctionalInterface
    public interface AfterInit {
        void afterInit(MinecraftClient client, Screen screen, int scaledWidth, int scaledHeight);
    }

    @FunctionalInterface
    public interface Remove {
        void onRemove(Screen screen);
    }

    @FunctionalInterface
    public interface BeforeRender {
        void beforeRender(Screen screen, DrawContext drawContext, int mouseX, int mouseY, float tickDelta);
    }

    @FunctionalInterface
    public interface AfterRender {
        void afterRender(Screen screen, DrawContext drawContext, int mouseX, int mouseY, float tickDelta);
    }

    @FunctionalInterface
    public interface BeforeTick {
        void beforeTick(Screen screen);
    }

    @FunctionalInterface
    public interface AfterTick {
        void afterTick(Screen screen);
    }

    //--

    public static Event<AllowMouseClick> allowMouseClick(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).owo_getAllowMouseClickEvent();
    }

    public static Event<BeforeMouseClick> beforeMouseClick(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).owo_getBeforeMouseClickEvent();
    }

    public static Event<AfterMouseClick> afterMouseClick(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).owo_getAfterMouseClickEvent();
    }

    public static Event<AllowMouseRelease> allowMouseRelease(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).owo_getAllowMouseReleaseEvent();
    }

    public static Event<BeforeMouseRelease> beforeMouseRelease(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).owo_getBeforeMouseReleaseEvent();
    }

    public static Event<AfterMouseRelease> afterMouseRelease(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).owo_getAfterMouseReleaseEvent();
    }

    public static Event<AllowMouseScroll> allowMouseScroll(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).owo_getAllowMouseScrollEvent();
    }

    public static Event<BeforeMouseScroll> beforeMouseScroll(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).owo_getBeforeMouseScrollEvent();
    }

    public static Event<AfterMouseScroll> afterMouseScroll(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).owo_getAfterMouseScrollEvent();
    }

    @FunctionalInterface
    public interface AllowMouseClick {
        boolean allowMouseClick(Screen screen, double mouseX, double mouseY, int button);
    }

    @FunctionalInterface
    public interface BeforeMouseClick {
        void beforeMouseClick(Screen screen, double mouseX, double mouseY, int button);
    }

    @FunctionalInterface
    public interface AfterMouseClick {
        void afterMouseClick(Screen screen, double mouseX, double mouseY, int button);
    }

    @FunctionalInterface
    public interface AllowMouseRelease {
        boolean allowMouseRelease(Screen screen, double mouseX, double mouseY, int button);
    }

    @FunctionalInterface
    public interface BeforeMouseRelease {
        void beforeMouseRelease(Screen screen, double mouseX, double mouseY, int button);
    }

    @FunctionalInterface
    public interface AfterMouseRelease {
        void afterMouseRelease(Screen screen, double mouseX, double mouseY, int button);
    }

    @FunctionalInterface
    public interface AllowMouseScroll {
        boolean allowMouseScroll(Screen screen, double mouseX, double mouseY, double horizontalAmount, double verticalAmount);
    }

    @FunctionalInterface
    public interface BeforeMouseScroll {
        void beforeMouseScroll(Screen screen, double mouseX, double mouseY, double horizontalAmount, double verticalAmount);
    }

    @FunctionalInterface
    public interface AfterMouseScroll {
        void afterMouseScroll(Screen screen, double mouseX, double mouseY, double horizontalAmount, double verticalAmount);
    }

    //--

    public static Event<AllowKeyPress> allowKeyPress(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).owo_getAllowKeyPressEvent();
    }

    public static Event<BeforeKeyPress> beforeKeyPress(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).owo_getBeforeKeyPressEvent();
    }

    public static Event<AfterKeyPress> afterKeyPress(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).owo_getAfterKeyPressEvent();
    }

    public static Event<AllowKeyRelease> allowKeyRelease(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).owo_getAllowKeyReleaseEvent();
    }

    public static Event<BeforeKeyRelease> beforeKeyRelease(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).owo_getBeforeKeyReleaseEvent();
    }

    public static Event<AfterKeyRelease> afterKeyRelease(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).owo_getAfterKeyReleaseEvent();
    }

    @FunctionalInterface
    public interface AllowKeyPress {
        boolean allowKeyPress(Screen screen, int key, int scancode, int modifiers);
    }

    @FunctionalInterface
    public interface BeforeKeyPress {
        void beforeKeyPress(Screen screen, int key, int scancode, int modifiers);
    }

    @FunctionalInterface
    public interface AfterKeyPress {
        void afterKeyPress(Screen screen, int key, int scancode, int modifiers);
    }

    @FunctionalInterface
    public interface AllowKeyRelease {
        boolean allowKeyRelease(Screen screen, int key, int scancode, int modifiers);
    }

    @FunctionalInterface
    public interface BeforeKeyRelease {
        void beforeKeyRelease(Screen screen, int key, int scancode, int modifiers);
    }

    @FunctionalInterface
    public interface AfterKeyRelease {
        void afterKeyRelease(Screen screen, int key, int scancode, int modifiers);
    }

    //--

    public interface ScreenExtensions {
        static ScreenExtensions getExtensions(Screen screen) {
            return (ScreenExtensions) screen;
        }

        Event<ScreenEvents.Remove> owo_getRemoveEvent();

        Event<ScreenEvents.BeforeTick> owo_getBeforeTickEvent();

        Event<ScreenEvents.AfterTick> owo_getAfterTickEvent();

        Event<ScreenEvents.BeforeRender> owo_getBeforeRenderEvent();

        Event<ScreenEvents.AfterRender> owo_getAfterRenderEvent();

        // Keyboard

        Event<AllowKeyPress> owo_getAllowKeyPressEvent();

        Event<BeforeKeyPress> owo_getBeforeKeyPressEvent();

        Event<AfterKeyPress> owo_getAfterKeyPressEvent();

        Event<AllowKeyRelease> owo_getAllowKeyReleaseEvent();

        Event<BeforeKeyRelease> owo_getBeforeKeyReleaseEvent();

        Event<AfterKeyRelease> owo_getAfterKeyReleaseEvent();

        // Mouse

        Event<AllowMouseClick> owo_getAllowMouseClickEvent();

        Event<BeforeMouseClick> owo_getBeforeMouseClickEvent();

        Event<AfterMouseClick> owo_getAfterMouseClickEvent();

        Event<AllowMouseRelease> owo_getAllowMouseReleaseEvent();

        Event<BeforeMouseRelease> owo_getBeforeMouseReleaseEvent();

        Event<AfterMouseRelease> owo_getAfterMouseReleaseEvent();

        Event<AllowMouseScroll> owo_getAllowMouseScrollEvent();

        Event<BeforeMouseScroll> owo_getBeforeMouseScrollEvent();

        Event<AfterMouseScroll> owo_getAfterMouseScrollEvent();
    }
}
