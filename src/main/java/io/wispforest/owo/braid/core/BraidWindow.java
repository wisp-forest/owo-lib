package io.wispforest.owo.braid.core;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.device.GpuSurface;
import com.mojang.renderpearl.api.device.SurfaceException;
import com.mojang.renderpearl.backend.opengl.GlTexture;
import io.wispforest.owo.Owo;
import io.wispforest.owo.braid.core.cursor.CursorController;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.core.events.*;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.util.BraidGuiRenderer;
import io.wispforest.owo.mixin.braid.MinecraftAccessor;
import io.wispforest.owo.mixin.braid.RenderSystemAccessor;
import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.EventStream;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL32;
import org.lwjgl.sdl.SDLError;
import org.lwjgl.sdl.SDLEvents;
import org.lwjgl.sdl.SDLKeyboard;
import org.lwjgl.sdl.SDLVideo;
import org.lwjgl.sdl.SDL_Event;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Supplier;

// TODO: consider somehow getting notified or polling
//       for changes in the gui scale option so we can react
//       instantly when it changes rather than on next resize
public class BraidWindow implements Surface {

    private static final Long2ObjectMap<BraidWindow> OPEN_WINDOWS = new Long2ObjectOpenHashMap<>();

    public final EventBinding eventBinding = new WindowEventBinding(this);

    public final Window backendWindow;
    private final Swapchain swapchain;

    private final List<String> pendingDroppedFiles = new ArrayList<>();

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
                    remoteTarget.destroyBuffers();
                    remoteTarget = new TextureTarget("braid window", backendWindow.getWidth(), backendWindow.getHeight(), GpuFormat.RGBA8_UNORM, GpuFormat.D32_FLOAT);

                    onResize.sink().onResize(backendWindow.getGuiScaledWidth(), backendWindow.getGuiScaledHeight());

                    resizeSwapchain();
                }

                @Override
                public void resizeGui() {}

                @Override
                public void cursorEntered() {}

                @Override
                public void fullscreenStateChanged(boolean fullscreen) {}
            },
            new DisplayData(width, height, OptionalInt.empty(), OptionalInt.empty(), false),
            null,
            false,
            title,
            ((MinecraftAccessor) Minecraft.getInstance()).owo$getMonitorManager(),
            RenderSystemAccessor.owo$getBackend()
        );

        SDLVideo.SDL_ShowWindow(this.backendWindow.handle());
        SDLKeyboard.SDL_StartTextInput(this.backendWindow.handle());

        this.swapchain = IS_VULKAN.get()
            ? new VulkanSwapchain()
            : new GlSwapchain();

        this.cursorController = new CursorController(this.backendWindow.handle());
        this.guiRenderer = new BraidGuiRenderer(Minecraft.getInstance());

        this.remoteTarget = new TextureTarget("braid window", this.backendWindow.getWidth(), this.backendWindow.getHeight(), GpuFormat.RGBA8_UNORM, GpuFormat.D32_FLOAT);
        this.resizeSwapchain();

        OPEN_WINDOWS.put(this.backendWindow.handle(), this);
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

    @ApiStatus.Internal
    public static boolean dispatchEvent(SDL_Event event) {
        if (OPEN_WINDOWS.isEmpty()) return false;

        var window = OPEN_WINDOWS.get(SDLEvents.SDL_GetWindowFromEvent(event));
        if (window == null) return false;

        window.handleEvent(event);
        return true;
    }

    private void handleEvent(SDL_Event event) {
        switch (event.type()) {
            case SDLEvents.SDL_EVENT_WINDOW_CLOSE_REQUESTED -> this.eventBinding.add(CloseEvent.INSTANCE);
            case SDLEvents.SDL_EVENT_KEY_DOWN -> {
                var key = event.key();
                this.eventBinding.add(new KeyPressEvent(key.scancode(), key.key(), new KeyModifiers(key.mod())));
            }
            case SDLEvents.SDL_EVENT_KEY_UP -> {
                var key = event.key();
                this.eventBinding.add(new KeyReleaseEvent(key.scancode(), key.key(), new KeyModifiers(key.mod())));
            }
            case SDLEvents.SDL_EVENT_TEXT_INPUT -> {
                var text = event.text().textString();
                if (text == null) return;

                var modifiers = new KeyModifiers(SDLKeyboard.SDL_GetModState());
                text.codePoints().forEach(codepoint -> this.eventBinding.add(new CharInputEvent((char) codepoint, modifiers)));
            }
            case SDLEvents.SDL_EVENT_TEXT_EDITING -> {}
            case SDLEvents.SDL_EVENT_MOUSE_MOTION -> {
                var motion = event.motion();
                this.eventBinding.add(new MouseMoveEvent(
                    (double) motion.x() / this.scaleFactor,
                    (double) motion.y() / this.scaleFactor
                ));
            }
            case SDLEvents.SDL_EVENT_MOUSE_BUTTON_DOWN -> this.eventBinding.add(new MouseButtonPressEvent(event.button().button(), new KeyModifiers(SDLKeyboard.SDL_GetModState())));
            case SDLEvents.SDL_EVENT_MOUSE_BUTTON_UP -> this.eventBinding.add(new MouseButtonReleaseEvent(event.button().button(), new KeyModifiers(SDLKeyboard.SDL_GetModState())));
            case SDLEvents.SDL_EVENT_MOUSE_WHEEL -> {
                var wheel = event.wheel();
                this.eventBinding.add(new MouseScrollEvent(wheel.x(), wheel.y()));
            }
            case SDLEvents.SDL_EVENT_DROP_BEGIN -> this.pendingDroppedFiles.clear();
            case SDLEvents.SDL_EVENT_DROP_FILE -> {
                var pathString = event.drop().dataString();
                if (pathString != null) this.pendingDroppedFiles.add(pathString);
            }
            case SDLEvents.SDL_EVENT_DROP_COMPLETE -> this.dispatchDroppedFiles();
            default -> this.backendWindow.handleEvent(event);
        }
    }

    private void dispatchDroppedFiles() {
        var paths = new ArrayList<Path>(this.pendingDroppedFiles.size());

        for (var pathString : this.pendingDroppedFiles) {
            try {
                paths.add(Paths.get(pathString));
            } catch (InvalidPathException e) {
                Owo.LOGGER.error("Failed to parse path '{}'", pathString, e);
            }
        }

        this.pendingDroppedFiles.clear();

        if (!paths.isEmpty()) {
            this.eventBinding.add(new FilesDroppedEvent(paths));
        }
    }

    // ---

    @Override
    public void dispose() {
        OPEN_WINDOWS.remove(this.backendWindow.handle());

        this.swapchain.dispose();
        this.backendWindow.close();
        this.cursorController.dispose();

        this.guiRenderer.close();

        this.remoteTarget.destroyBuffers();
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
            RenderSystem.DEFAULT_DEPTH_CLEAR_VALUE
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

    private static void withContext(long windowHandle, long contextHandle, Runnable fn) {
        var previousWindow = SDLVideo.SDL_GL_GetCurrentWindow();
        var previousContext = SDLVideo.SDL_GL_GetCurrentContext();

        try {
            SDLVideo.SDL_GL_MakeCurrent(windowHandle, contextHandle);
            fn.run();
        } finally {
            SDLVideo.SDL_GL_MakeCurrent(previousWindow, previousContext);
        }
    }

    private static final Supplier<Boolean> IS_VULKAN = Suppliers.memoize(() -> !RenderSystem.getDevice().getDeviceInfo().backendName().equals("OpenGL"));

    // ---

    public static class WindowEventBinding extends EventBinding {

        public final BraidWindow window;

        public WindowEventBinding(BraidWindow window) {
            this.window = window;
        }

        @Override
        public boolean isKeyPressed(int keyCode) {
            return InputConstants.isKeyDown(keyCode);
        }
    }

    public record OpenResult(AppState state, BraidWindow window) {}

    private sealed interface Swapchain permits GlSwapchain, VulkanSwapchain {
        void prepareFrame();
        void present();

        void resize();
        void dispose();
    }

    private final class GlSwapchain implements Swapchain {

        private final long context;
        private int localFbo;

        private GlSwapchain() {
            var previousWindow = SDLVideo.SDL_GL_GetCurrentWindow();
            var previousContext = SDLVideo.SDL_GL_GetCurrentContext();

            SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_SHARE_WITH_CURRENT_CONTEXT, 1);
            this.context = SDLVideo.SDL_GL_CreateContext(backendWindow.handle());
            var error = SDLError.SDL_GetError();
            if (this.context != 0) SDLVideo.SDL_GL_SetSwapInterval(0);
            SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_SHARE_WITH_CURRENT_CONTEXT, 0);

            SDLVideo.SDL_GL_MakeCurrent(previousWindow, previousContext);

            if (this.context == 0) {
                throw new UnsupportedOperationException("Failed to create braid window GL context: " + error);
            }
        }

        @Override
        public void prepareFrame() {}

        @Override
        public void present() {
            withContext(backendWindow.handle(), this.context, () -> {
                GL32.glBindFramebuffer(GL32.GL_READ_FRAMEBUFFER, this.localFbo);
                GL32.glBindFramebuffer(GL32.GL_DRAW_FRAMEBUFFER, 0);

                GL32.glBlitFramebuffer(
                    0, 0, backendWindow.getWidth(), backendWindow.getHeight(),
                    0, 0, backendWindow.getWidth(), backendWindow.getHeight(),
                    GL32.GL_COLOR_BUFFER_BIT,
                    GL32.GL_NEAREST
                );

                SDLVideo.SDL_GL_SwapWindow(backendWindow.handle());
            });
        }

        @Override
        public void resize() {
            withContext(backendWindow.handle(), this.context, () -> {
                if (this.localFbo != 0) {
                    GL32.glDeleteFramebuffers(this.localFbo);
                }

                this.localFbo = GL32.glGenFramebuffers();
                GL32.glBindFramebuffer(GL32.GL_FRAMEBUFFER, this.localFbo);
                GL32.glFramebufferTexture2D(GL32.GL_FRAMEBUFFER, GL32.GL_COLOR_ATTACHMENT0, GL32.GL_TEXTURE_2D, ((GlTexture) remoteTarget.getColorTexture()).glId(), 0);

                if (GL32.glCheckFramebufferStatus(GL32.GL_FRAMEBUFFER) != GL32.GL_FRAMEBUFFER_COMPLETE) {
                    throw new UnsupportedOperationException("Failed to initialize local FBO");
                }
            });
        }

        @Override
        public void dispose() {
            withContext(backendWindow.handle(), this.context, () -> GL32.glDeleteFramebuffers(this.localFbo));
            SDLVideo.SDL_GL_DestroyContext(this.context);
        }
    }

    private final class VulkanSwapchain implements Swapchain {

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

            this.backendWindowSurface.blitFromTexture(
                RenderSystem.getDevice().createCommandEncoder(),
                remoteTarget.getColorTextureView()
            );

            RenderSystem.getDevice().createCommandEncoder().submit();
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