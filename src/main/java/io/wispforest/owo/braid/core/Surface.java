package io.wispforest.owo.braid.core;

import io.wispforest.owo.braid.core.cursor.CursorController;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.ui.event.WindowResizeCallback;
import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.EventStream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;

public interface Surface {

    int width();
    int height();
    double scaleFactor();

    EventSource<ResizeCallback> onResize();

    CursorStyle currentCursorStyle();
    void setCursorStyle(CursorStyle style);

    void beginRendering();
    void endRendering();

    void dispose();

    class Default implements Surface {

        private static EventStream<ResizeCallback> resizeEvents;

        private final Window window;
        private final CursorController cursorController;

        public Default() {
            this.window = MinecraftClient.getInstance().getWindow();
            this.cursorController = new CursorController(this.window.getHandle());

            if (resizeEvents == null) {
                resizeEvents = ResizeCallback.newStream();

                WindowResizeCallback.EVENT.register((client, resizedWindow) -> {
                    resizeEvents.sink().onResize(resizedWindow.getScaledWidth(), resizedWindow.getScaledHeight());
                });
            }
        }

        @Override
        public int width() {
            return this.window.getScaledWidth();
        }

        @Override
        public int height() {
            return this.window.getScaledHeight();
        }

        @Override
        public double scaleFactor() {
            return this.window.getScaleFactor();
        }

        @Override
        public EventSource<ResizeCallback> onResize() {
            return resizeEvents.source();
        }

        @Override
        public CursorStyle currentCursorStyle() {
            return this.cursorController.currentStyle();
        }

        @Override
        public void setCursorStyle(CursorStyle style) {
            this.cursorController.setStyle(style);
        }

        @Override
        public void beginRendering() {}

        @Override
        public void endRendering() {}

        @Override
        public void dispose() {
            this.cursorController.dispose();
        }
    }

    interface ResizeCallback {
        void onResize(int newWidth, int newHeight);

        static EventStream<ResizeCallback> newStream() {
            return new EventStream<>(callbacks -> (newWidth, newHeight) -> {
                for (var callback : callbacks) {
                    callback.onResize(newWidth, newHeight);
                }
            });
        }
    }
}
