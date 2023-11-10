package io.wispforest.owo.ui.window.context;

import io.wispforest.owo.util.InfallibleCloseable;

public final class CurrentWindowContext {
    private static WindowContext CURRENT = VanillaWindowContext.MAIN;

    private CurrentWindowContext() {

    }

    public static InfallibleCloseable setCurrent(WindowContext window) {
        var old = CURRENT;

        CURRENT = window;

        return () -> CURRENT = old;
    }

    public static WindowContext current() {
        return CURRENT;
    }
}
