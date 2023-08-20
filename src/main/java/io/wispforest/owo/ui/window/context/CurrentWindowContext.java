package io.wispforest.owo.ui.window.context;

public final class CurrentWindowContext {
    private static WindowContext CURRENT = VanillaWindowContext.MAIN;

    private CurrentWindowContext() {

    }

    public static WindowResetter setCurrent(WindowContext window) {
        var old = CURRENT;
        CURRENT = window;
        return new WindowResetter(old);
    }

    public static WindowContext current() {
        return CURRENT;
    }

    public static class WindowResetter implements AutoCloseable {
        private final WindowContext window;

        private WindowResetter(WindowContext window) {
            this.window = window;
        }

        @Override
        public void close() {
            CurrentWindowContext.CURRENT = window;
        }
    }
}
