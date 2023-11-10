package io.wispforest.owo.ui.window;

import io.wispforest.owo.util.InfallibleCloseable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class OpenWindows {
    private static final List<OwoWindow<?>> WINDOWS = new CopyOnWriteArrayList<>();
    private static final List<OwoWindow<?>> WINDOWS_VIEW = Collections.unmodifiableList(WINDOWS);

    private OpenWindows() {

    }

    static InfallibleCloseable add(OwoWindow<?> window) {
        WINDOWS.add(window);

        return () -> WINDOWS.remove(window);
    }

    public static @UnmodifiableView List<OwoWindow<?>> windows() {
        return WINDOWS_VIEW;
    }

    @ApiStatus.Internal
    public static void renderAll() {
        for (OwoWindow<?> window : WINDOWS) {
            window.render();
        }
    }
}
