package io.wispforest.owo.ui.window.context;

import io.wispforest.owo.ui.event.WindowResizeCallback;
import io.wispforest.owo.ui.window.WindowFramebufferResized;
import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.EventStream;
import io.wispforest.owo.util.SupportsFeaturesImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;

public class VanillaWindowContext extends SupportsFeaturesImpl<WindowContext> implements WindowContext {
    public static final VanillaWindowContext MAIN = new VanillaWindowContext(MinecraftClient.getInstance().getWindow());

    private final Window window;

    private final EventStream<WindowFramebufferResized> framebufferResizedEvents = WindowFramebufferResized.newStream();

    public VanillaWindowContext(Window window) {
        this.window = window;

        WindowResizeCallback.EVENT.register((client, window1) -> {
            if (window != window1) return;

            framebufferResizedEvents.sink().onFramebufferResized(window1.getFramebufferWidth(), window1.getFramebufferHeight());
        });
    }

    @Override
    public int framebufferWidth() {
        return window.getFramebufferWidth();
    }

    @Override
    public int framebufferHeight() {
        return window.getFramebufferHeight();
    }

    @Override
    public EventSource<WindowFramebufferResized> framebufferResized() {
        return framebufferResizedEvents.source();
    }

    @Override
    public int scaledWidth() {
        return window.getScaledWidth();
    }

    @Override
    public int scaledHeight() {
        return window.getScaledHeight();
    }

    @Override
    public double scaleFactor() {
        return window.getScaleFactor();
    }

    @Override
    public long handle() {
        return window.getHandle();
    }
}
