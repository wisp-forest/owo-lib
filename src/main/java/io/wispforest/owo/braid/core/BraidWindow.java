package io.wispforest.owo.braid.core;

import com.mojang.blaze3d.opengl.GlDebug;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.Owo;
import io.wispforest.owo.braid.core.cursor.CursorController;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.core.events.*;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.util.BraidGuiRenderer;
import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.EventStream;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.mutable.MutableLong;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL32;
import org.lwjgl.system.NativeResource;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

// TODO: consider somehow getting notified or polling
//       for changes in the gui scale option so we can react
//       instantly when it changes rather than on next resize
public class BraidWindow implements Surface {

    public final EventBinding eventBinding = new WindowEventBinding(this);

    public final long handle;
    private final List<NativeResource> resources = new ArrayList<>();

    private final EventStream<ResizeCallback> onResize = ResizeCallback.newStream();
    private TextureTarget remoteTarget;
    private int localFbo;

    public final BraidGuiRenderer guiRenderer;

    private final CursorController cursorController;

    private int framebufferWidth;
    private int framebufferHeight;

    private int scaledWidth;
    private int scaledHeight;
    private int scaleFactor;

    public BraidWindow(long handle) {
        this.handle = handle;
        this.cursorController = new CursorController(this.handle);

        this.guiRenderer = new BraidGuiRenderer(Minecraft.getInstance());

        var framebufferWidthOut = new int[1];
        var framebufferHeightOut = new int[1];
        GLFW.glfwGetFramebufferSize(this.handle, framebufferWidthOut, framebufferHeightOut);

        this.framebufferWidth = framebufferWidthOut[0];
        this.framebufferHeight = framebufferHeightOut[0];
        this.remoteTarget = new TextureTarget("braid window", this.framebufferWidth, this.framebufferHeight, true);
        this.recreateLocalFbo();

        GLFW.glfwSetWindowCloseCallback(this.handle, this.storeNativeResource(GLFWWindowCloseCallback.create(window -> {
            this.eventBinding.add(CloseEvent.INSTANCE);
        })));

        GLFW.glfwSetFramebufferSizeCallback(this.handle, this.storeNativeResource(GLFWFramebufferSizeCallback.create((window, width, height) -> {
            this.framebufferWidth = width;
            this.framebufferHeight = height;

            withContext(Minecraft.getInstance().getWindow().handle(), () -> {
                this.remoteTarget.destroyBuffers();
                this.remoteTarget = new TextureTarget("braid window", this.framebufferWidth, this.framebufferHeight, true);
            });

            this.recreateLocalFbo();

            this.onResize.sink().onResize(this.scaledWidth, this.scaledHeight);
        })));

        GLFW.glfwSetMouseButtonCallback(this.handle, this.storeNativeResource(GLFWMouseButtonCallback.create((window, button, action, mods) -> {
            this.eventBinding.add(switch (action) {
                case GLFW.GLFW_PRESS -> new MouseButtonPressEvent(button, new KeyModifiers(mods));
                case GLFW.GLFW_RELEASE -> new MouseButtonReleaseEvent(button, new KeyModifiers(mods));
                default -> throw new UnsupportedOperationException("incompatible glfw event type");
            });
        })));

        GLFW.glfwSetCursorPosCallback(this.handle, this.storeNativeResource(GLFWCursorPosCallback.create((window, mouseX, mouseY) -> {
            this.eventBinding.add(new MouseMoveEvent(
                mouseX / this.scaleFactor,
                mouseY / this.scaleFactor
            ));
        })));

        GLFW.glfwSetScrollCallback(this.handle, this.storeNativeResource(GLFWScrollCallback.create((window, xOffset, yOffset) -> {
            this.eventBinding.add(new MouseScrollEvent(xOffset, yOffset));
        })));

        GLFW.glfwSetKeyCallback(this.handle, this.storeNativeResource(GLFWKeyCallback.create((window, key, scancode, action, mods) -> {
            this.eventBinding.add(switch (action) {
                case GLFW.GLFW_PRESS, GLFW.GLFW_REPEAT -> new KeyPressEvent(key, scancode, new KeyModifiers(mods));
                case GLFW.GLFW_RELEASE -> new KeyReleaseEvent(key, scancode, new KeyModifiers(mods));
                default -> throw new UnsupportedOperationException("incompatible glfw event type");
            });
        })));

        GLFW.glfwSetCharModsCallback(this.handle, this.storeNativeResource(GLFWCharModsCallback.create((window, codepoint, mods) -> {
            this.eventBinding.add(new CharInputEvent((char) codepoint, new KeyModifiers(mods)));
        })));

        GLFW.glfwSetDropCallback(this.handle, this.storeNativeResource(GLFWDropCallback.create((window, count, names) -> {
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

    private void recreateLocalFbo() {
        withContext(this.handle, () -> {
            if (this.localFbo != 0) {
                GL32.glDeleteFramebuffers(this.localFbo);
            }

            this.localFbo = GL32.glGenFramebuffers();
            GL32.glBindFramebuffer(GL32.GL_FRAMEBUFFER, this.localFbo);
            GL32.glFramebufferTexture2D(GL32.GL_FRAMEBUFFER, GL32.GL_COLOR_ATTACHMENT0, GL32.GL_TEXTURE_2D, ((GlTexture) this.remoteTarget.getColorTexture()).glId(), 0);

            if (GL32.glCheckFramebufferStatus(GL32.GL_FRAMEBUFFER) != GL32.GL_FRAMEBUFFER_COMPLETE) {
                throw new UnsupportedOperationException("Failed to initialize local FBO");
            }
        });

        this.recalculateScale();
    }

    private void recalculateScale() {
        var guiScale = Minecraft.getInstance().options.guiScale().get();
        var forceUnicodeFont = Minecraft.getInstance().options.forceUnicodeFont().get();

        var factor = 1;

        while (
            factor != guiScale
                && factor < this.framebufferWidth
                && factor < this.framebufferHeight
                && this.framebufferWidth / (factor + 1) >= 320
                && this.framebufferHeight / (factor + 1) >= 240
        ) {
            ++factor;
        }

        if (forceUnicodeFont && factor % 2 != 0) {
            ++factor;
        }

        this.scaleFactor = factor;

        var scaledWidth = (int) ((double) this.framebufferWidth / this.scaleFactor);
        this.scaledWidth = (double) this.framebufferWidth / this.scaleFactor > (double) scaledWidth ? scaledWidth + 1 : scaledWidth;

        var scaledHeight = (int) ((double) this.framebufferHeight / this.scaleFactor);
        this.scaledHeight = (double) this.framebufferHeight / this.scaleFactor > (double) scaledHeight ? scaledHeight + 1 : scaledHeight;
    }

    public static BraidWindow create(String title, int width, int height) {
        var handleOut = new MutableLong();
        withContext(0, () -> {
            GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_OPENGL_API);
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_CREATION_API, GLFW.GLFW_NATIVE_CONTEXT_API);
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

            var handle = GLFW.glfwCreateWindow(width, height, title, 0, Minecraft.getInstance().getWindow().handle());

            if (handle == 0) {
                throw new UnsupportedOperationException("Failed to create a GLFW window");
            }

            GLFW.glfwMakeContextCurrent(handle);
            GLFW.glfwSwapInterval(0);

            GlDebug.enableDebugCallback(Minecraft.getInstance().options.glDebugVerbosity, true, new HashSet<>());

            handleOut.setValue(handle);
        });

        return new BraidWindow(handleOut.longValue());
    }

    public static OpenResult open(String title, int width, int height, Widget widget) {
        var window = create(title, width, height);
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
        GLFW.glfwDestroyWindow(this.handle);
        this.cursorController.dispose();

        this.guiRenderer.close();

        this.remoteTarget.destroyBuffers();

        for (var resource : this.resources) {
            resource.free();
        }
    }

    // ---

    @Override
    public int width() {
        return this.scaledWidth;
    }

    @Override
    public int height() {
        return this.scaledHeight;
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
        RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(
            this.remoteTarget.getColorTexture(),
            0xFF000000,
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

        withContext(this.handle, () -> {
            GL32.glBindFramebuffer(GL32.GL_READ_FRAMEBUFFER, this.localFbo);
            GL32.glBindFramebuffer(GL32.GL_DRAW_FRAMEBUFFER, 0);

            GL32.glBlitFramebuffer(
                0, 0, this.framebufferWidth, this.framebufferHeight,
                0, 0, this.framebufferWidth, this.framebufferHeight,
                GL32.GL_COLOR_BUFFER_BIT,
                GL32.GL_NEAREST
            );

            GLFW.glfwSwapBuffers(this.handle);
        });
    }

    // ---

    private <R extends NativeResource> R storeNativeResource(R resource) {
        this.resources.add(resource);
        return resource;
    }

    public static void withContext(long contextHandle, Runnable fn) {
        var activeContext = GLFW.glfwGetCurrentContext();

        try {
            GLFW.glfwMakeContextCurrent(contextHandle);
            fn.run();
        } finally {
            GLFW.glfwMakeContextCurrent(activeContext);
        }
    }

    // ---

    public static class WindowEventBinding extends EventBinding {

        public final BraidWindow window;

        public WindowEventBinding(BraidWindow window) {
            this.window = window;
        }

        @Override
        public boolean isKeyPressed(int keyCode) {
            return GLFW.glfwGetKey(this.window.handle, keyCode) == GLFW.GLFW_PRESS;
        }
    }

    public record OpenResult(AppState state, BraidWindow window) {}
}
