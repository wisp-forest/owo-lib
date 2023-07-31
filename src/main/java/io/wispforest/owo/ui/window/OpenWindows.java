package io.wispforest.owo.ui.window;

import org.jetbrains.annotations.ApiStatus;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class OpenWindows {
    private static final List<OwoWindow<?>> WINDOWS = new CopyOnWriteArrayList<>();

    private OpenWindows() {

    }

    @ApiStatus.Internal
    public static WindowRegistration add(OwoWindow<?> window) {
        WINDOWS.add(window);

        return new WindowRegistration(window);
    }

    @ApiStatus.Internal
    public static void renderAll() {
        for (OwoWindow<?> window : WINDOWS) {
            window.render();
        }
    }

    public static class WindowRegistration implements AutoCloseable {
        private final OwoWindow<?> window;

        private WindowRegistration(OwoWindow<?> window) {
            this.window = window;
        }

        @Override
        public void close() {
            WINDOWS.remove(window);
        }
    }
}
