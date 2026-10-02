package io.wispforest.owo.braid.core;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import com.mojang.renderpearl.api.device.GpuSurface;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.device.SurfaceException;
import io.wispforest.owo.Owo;
import io.wispforest.owo.braid.core.cursor.CursorController;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.core.events.*;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.util.BraidGuiRenderer;
import io.wispforest.owo.mixin.braid.MinecraftAccessor;
import io.wispforest.owo.mixin.braid.WindowMixin;
import org.lwjgl.sdl.*;
import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.EventStream;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector4f;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

// TODO: consider somehow getting notified or polling
//       for changes in the gui scale option so we can react
//       instantly when it changes rather than on next resize
public class BraidWindow implements Surface {

    public final EventBinding eventBinding = new WindowEventBinding(this);

    public final Window backendWindow;
    private final Swapchain swapchain;

    private final List<Path> droppedPaths = new ArrayList<>();
    private final SDL_EventFilter eventWatch;
    private boolean disposed;

    private final EventStream<ResizeCallback> onResize = ResizeCallback.newStream();
    private TextureTarget remoteTarget;

    public final BraidGuiRenderer guiRenderer;

    private final CursorController cursorController;

    private int scaleFactor;

    public BraidWindow(String title, int width, int height) {
        this.backendWindow = new Window(
            new WindowEventHandler() {
                @Override
                public void framebufferSizeChanged() {
                    if (remoteTarget == null) return;
                    remoteTarget.destroyBuffers();
                    remoteTarget = createTarget();
                    resizeSwapchain();
                    onResize.sink().onResize(backendWindow.getGuiScaledWidth(), backendWindow.getGuiScaledHeight());
                }

                @Override
                public void resizeGui() {}

                @Override
                public void cursorEntered() {
                    if (cursorController != null) cursorController.apply();
                }

                @Override
                public void fullscreenStateChanged(boolean fullscreen) {}
            },
            new DisplayData(width, height, OptionalInt.empty(), OptionalInt.empty(), false),
            null,
            false,
            title,
            ((MinecraftAccessor) Minecraft.getInstance()).owo$getMonitorManager(),
            WindowMixin.owo$getBackend());

        this.swapchain = new SurfaceSwapchain();
        this.cursorController = new CursorController(this.backendWindow.handle());
        this.guiRenderer = new BraidGuiRenderer(Minecraft.getInstance());
        this.remoteTarget = this.createTarget();
        this.resizeSwapchain();
        this.backendWindow.setWindowCloseCallback(() -> this.eventBinding.add(CloseEvent.INSTANCE));
        this.backendWindow.setAllowCursorChanges(false);

        // SDL event watches may run outside the render thread. Copy native-backed
        // data before returning, then dispatch all UI changes on Minecraft's thread.
        this.eventWatch = SDL_EventFilter.create((_, address) -> {
            var event = SDL_Event.create(address);
            if (SDLEvents.SDL_GetWindowFromEvent(event) != this.backendWindow.handle()) return true;
            this.captureEvent(event);
            return true;
        });
        if (!SDLEvents.SDL_AddEventWatch(this.eventWatch, 0)) {
            this.eventWatch.free();
            this.disposeResources();
            throw new IllegalStateException("Failed to register Braid window event watch");
        }
        SDLKeyboard.SDL_StartTextInput(this.backendWindow.handle());
        SDLVideo.SDL_ShowWindow(this.backendWindow.handle());
    }

    private TextureTarget createTarget() {
        return new TextureTarget("braid window", this.backendWindow.getWidth(), this.backendWindow.getHeight(), GpuFormat.RGBA8_UNORM, GpuFormat.D32_FLOAT);
    }

    private void dispatch(Runnable task) {
        Minecraft.getInstance().execute(() -> {
            if (!this.disposed) task.run();
        });
    }

    private void captureEvent(SDL_Event event) {
        var type = event.type();
        if (type >= SDLEvents.SDL_EVENT_WINDOW_FIRST && type <= SDLEvents.SDL_EVENT_WINDOW_LAST) {
            var copy = SDL_Event.malloc().set(event);
            Minecraft.getInstance().execute(() -> {
                try {
                    if (!this.disposed) this.backendWindow.handleEvent(copy);
                } finally {
                    copy.free();
                }
            });
            return;
        }
        switch (type) {
            case SDLEvents.SDL_EVENT_MOUSE_BUTTON_DOWN, SDLEvents.SDL_EVENT_MOUSE_BUTTON_UP -> {
                var button = Byte.toUnsignedInt(event.button().button());
                var modifiers = new KeyModifiers(Short.toUnsignedInt(SDLKeyboard.SDL_GetModState()));
                this.dispatch(() -> this.eventBinding.add(type == SDLEvents.SDL_EVENT_MOUSE_BUTTON_DOWN
                    ? new MouseButtonPressEvent(button, modifiers)
                    : new MouseButtonReleaseEvent(button, modifiers)));
            }
            case SDLEvents.SDL_EVENT_MOUSE_MOTION -> {
                var x = event.motion().x();
                var y = event.motion().y();
                this.dispatch(() -> this.eventBinding.add(new MouseMoveEvent(x / this.scaleFactor, y / this.scaleFactor)));
            }
            case SDLEvents.SDL_EVENT_MOUSE_WHEEL -> {
                var direction = event.wheel().direction() == SDLMouse.SDL_MOUSEWHEEL_FLIPPED ? -1 : 1;
                var x = event.wheel().x() * direction;
                var y = event.wheel().y() * direction;
                this.dispatch(() -> this.eventBinding.add(new MouseScrollEvent(x, y)));
            }
            case SDLEvents.SDL_EVENT_KEY_DOWN, SDLEvents.SDL_EVENT_KEY_UP -> {
                var key = event.key().scancode();
                var keycode = event.key().key();
                var modifiers = new KeyModifiers(Short.toUnsignedInt(event.key().mod()));
                this.dispatch(() -> this.eventBinding.add(type == SDLEvents.SDL_EVENT_KEY_DOWN
                    ? new KeyPressEvent(key, keycode, modifiers)
                    : new KeyReleaseEvent(key, keycode, modifiers)));
            }
            case SDLEvents.SDL_EVENT_TEXT_INPUT -> {
                var text = event.text().textString();
                var modifiers = new KeyModifiers(Short.toUnsignedInt(SDLKeyboard.SDL_GetModState()));
                this.dispatch(() -> {
                    for (var character : text.toCharArray()) this.eventBinding.add(new CharInputEvent(character, modifiers));
                });
            }
            case SDLEvents.SDL_EVENT_DROP_BEGIN -> this.dispatch(this.droppedPaths::clear);
            case SDLEvents.SDL_EVENT_DROP_FILE -> {
                var pathString = event.drop().dataString();
                this.dispatch(() -> {
                    try {
                        this.droppedPaths.add(Paths.get(pathString));
                    } catch (InvalidPathException e) {
                        Owo.LOGGER.error("Failed to parse dropped path", e);
                    }
                });
            }
            case SDLEvents.SDL_EVENT_DROP_COMPLETE -> this.dispatch(() -> {
                if (!this.droppedPaths.isEmpty()) this.eventBinding.add(new FilesDroppedEvent(List.copyOf(this.droppedPaths)));
                this.droppedPaths.clear();
            });
        }
    }

    private void resizeSwapchain() {
        this.swapchain.resize();
        this.recalculateScale();
    }

    private void recalculateScale() {
        var guiScale = Minecraft.getInstance().options.guiScale().get();
        var forceUnicodeFont = Minecraft.getInstance().options.forceUnicodeFont().get();

        this.scaleFactor = this.backendWindow.calculateScale(guiScale, forceUnicodeFont);
        this.backendWindow.setGuiScale(scaleFactor);
    }

    public static OpenResult open(String title, int width, int height, Widget widget) {
        var window = new BraidWindow(title, width, height);
        var app = new AppState(
            Owo.LOGGER,
            AppState.formatName("BraidWindow", widget, title),
            Minecraft.getInstance(),
            window,
            window.eventBinding,
            widget
        );

        BraidWindowScheduler.add(window, app);
        return new OpenResult(app, window);
    }

    // ---

    @Override
    public void dispose() {
        if (this.disposed) return;
        this.disposed = true;
        SDLEvents.SDL_RemoveEventWatch(this.eventWatch, 0);
        this.eventWatch.free();
        this.disposeResources();
    }

    private void disposeResources() {
        SDLKeyboard.SDL_StopTextInput(this.backendWindow.handle());
        this.swapchain.dispose();
        this.cursorController.dispose();
        this.guiRenderer.close();
        this.remoteTarget.destroyBuffers();
        this.backendWindow.close();
    }

    // ---

    @Override
    public int width() {
        return this.backendWindow.getGuiScaledWidth();
    }

    @Override
    public int height() {
        return this.backendWindow.getGuiScaledHeight();
    }

    @Override
    public double scaleFactor() {
        return this.scaleFactor;
    }

    @Override
    public EventSource<ResizeCallback> onResize() {
        return this.onResize.source();
    }

    @Override
    public CursorStyle currentCursorStyle() {
        return this.cursorController.currentStyle();
    }

    @Override
    public void setCursorStyle(CursorStyle style) {
        this.cursorController.setStyle(style);
    }

    // ---

    @Override
    public void beginRendering() {
        this.swapchain.prepareFrame();

        RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(
            this.remoteTarget.getColorTexture(),
            new Vector4f(0, 0, 0, 1),
            this.remoteTarget.getDepthTexture(),
            0 // Minecraft 26.3 uses reversed depth, matching GuiRenderer and GameRenderer.
        );
    }

    @Override
    public void endRendering() {
        this.guiRenderer.render(new BraidGuiRenderer.Target(
            this.remoteTarget,
            this
        ));

        // ---

        this.swapchain.present();
    }

    // ---

    /** @deprecated RenderPearl owns one shared device context for all surfaces. */
    @Deprecated
    @ApiStatus.Internal
    public static ThreadLocal<Boolean> SHARE_NEXT_WINDOW_INSTANCE = ThreadLocal.withInitial(() -> false);

    public static class WindowEventBinding extends EventBinding {

        public final BraidWindow window;

        public WindowEventBinding(BraidWindow window) {
            this.window = window;
        }

        @Override
        public boolean isKeyPressed(int keyCode) {
            return this.window.backendWindow.isFocused() && InputConstants.isKeyDown(keyCode);
        }
    }

    public record OpenResult(AppState state, BraidWindow window) {}

    private interface Swapchain {
        void prepareFrame();
        void present();
        void resize();
        void dispose();
    }

    private final class SurfaceSwapchain implements Swapchain {

        private final GpuSurface backendWindowSurface = RenderSystem.getDevice().createSurface(backendWindow.handle(), backendWindow::isIconified);
        private boolean surfaceValid = false;

        @Override
        public void prepareFrame() {
            if (!this.surfaceValid) {
                try {
                    this.backendWindowSurface.configure(new GpuSurface.Configuration(
                        backendWindow.getWidth(),
                        backendWindow.getHeight(),
                        GpuSurface.PresentMode.getSupportedVsyncMode(this.backendWindowSurface.supportedPresentModes(), false)
                    ));
                    this.surfaceValid = true;
                } catch (SurfaceException e) {
                    Owo.LOGGER.warn("Failed to resize braid window");
                }
            }

            if (!this.surfaceValid) {
                return;
            }

            try {
                this.backendWindowSurface.acquireNextTexture();
            } catch (SurfaceException e) {
                Owo.LOGGER.warn("Failed to acquire texture");
            }
        }

        @Override
        public void present() {
            if (!this.backendWindowSurface.isAcquired()) {
                return;
            }

            var encoder = RenderSystem.getDevice().createCommandEncoder();
            this.backendWindowSurface.blitFromTexture(encoder, remoteTarget.getColorTextureView());
            encoder.submit();
            this.backendWindowSurface.present();
        }

        @Override
        public void resize() {
            this.surfaceValid = false;
        }

        @Override
        public void dispose() {
            this.backendWindowSurface.close();
        }
    }
}
