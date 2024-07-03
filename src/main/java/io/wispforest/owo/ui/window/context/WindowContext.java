package io.wispforest.owo.ui.window.context;

import io.wispforest.owo.ui.window.WindowFramebufferResized;
import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.SupportsFeatures;
import net.minecraft.client.gl.Framebuffer;

public interface WindowContext extends SupportsFeatures<WindowContext> {
    int framebufferWidth();
    int framebufferHeight();
    EventSource<WindowFramebufferResized> framebufferResized();
    Framebuffer framebuffer();

    int scaledWidth();
    int scaledHeight();
    double scaleFactor();

    long handle();
}
