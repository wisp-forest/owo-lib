package io.wispforest.owo.neoforge.api.screen;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

import java.util.Objects;

public final class ScreenEvents {

    public static final Event<ScreenEvents.BeforeInit> BEFORE_INIT = EventFactory.createArrayBacked(ScreenEvents.BeforeInit.class, callbacks -> (client, screen, scaledWidth, scaledHeight) -> {
        for (ScreenEvents.BeforeInit callback : callbacks) {
            callback.beforeInit(client, screen, scaledWidth, scaledHeight);
        }
    });

    public static final Event<ScreenEvents.AfterInit> AFTER_INIT = EventFactory.createArrayBacked(ScreenEvents.AfterInit.class, callbacks -> (client, screen, scaledWidth, scaledHeight) -> {
        for (ScreenEvents.AfterInit callback : callbacks) {
            callback.afterInit(client, screen, scaledWidth, scaledHeight);
        }
    });

    static {
        ScreenExtensions.init();
    }

    public static Event<ScreenEvents.Remove> remove(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).fabric_getRemoveEvent();
    }

    public static Event<ScreenEvents.BeforeExtract> beforeExtract(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).fabric_getBeforeRenderEvent();
    }

    public static Event<ScreenEvents.AfterBackground> afterBackground(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).fabric_getAfterBackgroundEvent();
    }

    public static Event<ScreenEvents.AfterExtract> afterExtract(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).fabric_getAfterRenderEvent();
    }

    public static Event<ScreenEvents.BeforeTick> beforeTick(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).fabric_getBeforeTickEvent();
    }

    public static Event<ScreenEvents.AfterTick> afterTick(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");

        return ScreenExtensions.getExtensions(screen).fabric_getAfterTickEvent();
    }

    @FunctionalInterface
    public interface BeforeInit {
        void beforeInit(Minecraft client, Screen screen, int scaledWidth, int scaledHeight);
    }

    @FunctionalInterface
    public interface AfterInit {
        void afterInit(Minecraft client, Screen screen, int scaledWidth, int scaledHeight);
    }

    @FunctionalInterface
    public interface Remove {
        void onRemove(Screen screen);
    }

    @FunctionalInterface
    public interface BeforeExtract {
        void beforeExtract(Screen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float tickProgress);
    }

    @FunctionalInterface
    public interface AfterBackground {
        void afterBackground(Screen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float tickProgress);
    }

    @FunctionalInterface
    public interface AfterExtract {
        void afterExtract(Screen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float tickProgress);
    }

    @FunctionalInterface
    public interface BeforeTick {
        void beforeTick(Screen screen);
    }

    @FunctionalInterface
    public interface AfterTick {
        void afterTick(Screen screen);
    }
}

