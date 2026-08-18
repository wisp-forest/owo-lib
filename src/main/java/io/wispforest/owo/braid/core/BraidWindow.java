package io.wispforest.owo.braid.core;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import com.mojang.blaze3d.systems.BackendCreationException;
import com.mojang.blaze3d.systems.GpuSurface;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.SurfaceException;
import io.wispforest.owo.Owo;
import io.wispforest.owo.braid.core.cursor.CursorController;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.core.events.*;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.util.BraidGuiRenderer;
import io.wispforest.owo.mixin.braid.MinecraftAccessor;
import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.EventStream;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector4f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL32;
import org.lwjgl.system.NativeResource;

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

    public final EventBinding eventBinding = new WindowEventBinding(this);

    public final Window backendWindow;
    private final Swapchain swapchain;

    private final List<NativeResource> resources = new ArrayList<>();

    private final EventStream<ResizeCallback> onResize = ResizeCallback.newStream();
    private TextureTarget remoteTarget;

    public final BraidGuiRenderer guiRenderer;

    private final CursorController cursorController;

    private int scaleFactor;

    public BraidWindow(String title, int width, int height) {
        try {
            if (!IS_VULKAN.get()) {
                SHARE_NEXT_WINDOW_INSTANCE.set(true);
            }

            this.backendWindow = new Window(
                new WindowEventHandler() {
                    @Override
                    public void framebufferSizeChanged() {
                        withContext(Minecraft.getInstance().getWindow().handle(), () -> {
                            remoteTarget.destroyBuffers();
                            remoteTarget = new TextureTarget("braid window", backendWindow.getWidth(), backendWindow.getHeight(), true, GpuFormat.RGBA8_UNORM);
                        });

                        onResize.sink().onResize(backendWindow.getGuiScaledWidth(), backendWindow.getGuiScaledHeight());

                        resizeSwapchain();
                    }

                    @Override
                    public void resizeGui() {}

                    @Override
                    public void cursorEntered() {}
                },
                new DisplayData(width, height, OptionalInt.empty(), OptionalInt.empty(), false),
                null,
                false,
                title,
                ((MinecraftAccessor) Minecraft.getInstance()).owo$getMonitorManager(),
                Minecraft.getInstance().getWindow().backend());
        } catch (BackendCreationException e) {
            throw new UnsupportedOperationException("Failed to create backend window", e);
        }

        GLFW.glfwShowWindow(this.backendWindow.handle());

        this.swapchain = IS_VULKAN.get()
            ? new VulkanSwapchain()
            : new GlSwapchain();

        this.cursorController = new CursorController(this.backendWindow.handle());
        this.guiRenderer = new BraidGuiRenderer(Minecraft.getInstance());

        this.remoteTarget = new TextureTarget("braid window", this.backendWindow.getWidth(), this.backendWindow.getHeight(), true, GpuFormat.RGBA8_UNORM);
        this.resizeSwapchain();

        GLFW.glfwSetWindowCloseCallback(this.backendWindow.handle(), this.storeNativeResource(GLFWWindowCloseCallback.create(_ -> {
            this.eventBinding.add(CloseEvent.INSTANCE);
        })));

        GLFW.glfwSetMouseButtonCallback(this.backendWindow.handle(), this.storeNativeResource(GLFWMouseButtonCallback.create((_, button, action, mods) -> {
            this.eventBinding.add(switch (action) {
                case GLFW.GLFW_PRESS -> new MouseButtonPressEvent(button, new KeyModifiers(mods));
                case GLFW.GLFW_RELEASE -> new MouseButtonReleaseEvent(button, new KeyModifiers(mods));
                default -> throw new UnsupportedOperationException("incompatible glfw event type");
            });
        })));

        GLFW.glfwSetCursorPosCallback(this.backendWindow.handle(), this.storeNativeResource(GLFWCursorPosCallback.create((_, mouseX, mouseY) -> {
            this.eventBinding.add(new MouseMoveEvent(
                mouseX / this.scaleFactor,
                mouseY / this.scaleFactor
            ));
        })));

        GLFW.glfwSetScrollCallback(this.backendWindow.handle(), this.storeNativeResource(GLFWScrollCallback.create((_, xOffset, yOffset) -> {
            this.eventBinding.add(new MouseScrollEvent(xOffset, yOffset));
        })));

        GLFW.glfwSetKeyCallback(this.backendWindow.handle(), this.storeNativeResource(GLFWKeyCallback.create((_, key, scancode, action, mods) -> {
            this.eventBinding.add(switch (action) {
                case GLFW.GLFW_PRESS, GLFW.GLFW_REPEAT -> new KeyPressEvent(key, scancode, new KeyModifiers(mods));
                case GLFW.GLFW_RELEASE -> new KeyReleaseEvent(key, scancode, new KeyModifiers(mods));
                default -> throw new UnsupportedOperationException("incompatible glfw event type");
            });
        })));

        GLFW.glfwSetCharModsCallback(this.backendWindow.handle(), this.storeNativeResource(GLFWCharModsCallback.create((_, codepoint, mods) -> {
            this.eventBinding.add(new CharInputEvent((char) codepoint, new KeyModifiers(mods)));
        })));

        GLFW.glfwSetDropCallback(this.backendWindow.handle(), this.storeNativeResource(GLFWDropCallback.create((_, count, names) -> {
            var paths = new ArrayList<Path>(count);

            for (int pathIdx = 0; pathIdx < count; pathIdx++) {
                var pathString = GLFWDropCallback.getName(names, pathIdx);

                try {
                    paths.add(Paths.get(pathString));
                } catch (InvalidPathException e) {
                    Owo.LOGGER.error("Failed to parse path '{}'", pathString, e);
                }
            }

            if (!paths.isEmpty()) {
                this.eventBinding.add(new FilesDroppedEvent(paths));
            }
        })));
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
        this.backendWindow.close();
        this.swapchain.dispose();
        this.cursorController.dispose();

        this.guiRenderer.close();

        this.remoteTarget.destroyBuffers();

        for (var resource : this.resources) {
//            resource.free();
        }
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
            1
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

    private <R extends NativeResource> R storeNativeResource(R resource) {
        this.resources.add(resource);
        return resource;
    }

    private static void withContext(long contextHandle, Runnable fn) {
        if (IS_VULKAN.get()) {
            fn.run();
            return;
        }

        var activeContext = GLFW.glfwGetCurrentContext();

        try {
            GLFW.glfwMakeContextCurrent(contextHandle);
            fn.run();
        } finally {
            GLFW.glfwMakeContextCurrent(activeContext);
        }
    }

    private static final Supplier<Boolean> IS_VULKAN = Suppliers.memoize(() -> !RenderSystem.getDevice().getDeviceInfo().backendName().equals("OpenGL"));

    @ApiStatus.Internal
    public static ThreadLocal<Boolean> SHARE_NEXT_WINDOW_INSTANCE = Util.make(() -> {
        var tl = new ThreadLocal<Boolean>();
        tl.set(false);
        return tl;
    });

    // ---

    public static class WindowEventBinding extends EventBinding {

        public final BraidWindow window;

        public WindowEventBinding(BraidWindow window) {
            this.window = window;
        }

        @Override
        public boolean isKeyPressed(int keyCode) {
            return GLFW.glfwGetKey(this.window.backendWindow.handle(), keyCode) == GLFW.GLFW_PRESS;
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

        private int localFbo;

        @Override
        public void prepareFrame() {}

        @Override
        public void present() {
            withContext(backendWindow.handle(), () -> {
                GL32.glBindFramebuffer(GL32.GL_READ_FRAMEBUFFER, this.localFbo);
                GL32.glBindFramebuffer(GL32.GL_DRAW_FRAMEBUFFER, 0);

                GL32.glBlitFramebuffer(
                    0, 0, backendWindow.getWidth(), backendWindow.getHeight(),
                    0, 0, backendWindow.getWidth(), backendWindow.getHeight(),
                    GL32.GL_COLOR_BUFFER_BIT,
                    GL32.GL_NEAREST
                );

                GLFW.glfwSwapBuffers(backendWindow.handle());
            });
        }

        @Override
        public void resize() {
            withContext(backendWindow.handle(), () -> {
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
            GL32.glDeleteFramebuffers(this.localFbo);
        }
    }

    private final class VulkanSwapchain implements Swapchain {

        private final GpuSurface backendWindowSurface = RenderSystem.getDevice().createSurface(backendWindow.handle());
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
