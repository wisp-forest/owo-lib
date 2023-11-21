package io.wispforest.owo.ui.window;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.ui.event.CharTyped;
import io.wispforest.owo.ui.util.GlDebugUtils;
import io.wispforest.owo.ui.window.context.CurrentWindowContext;
import io.wispforest.owo.ui.window.context.WindowContext;
import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.EventStream;
import io.wispforest.owo.ui.util.OwoGlUtil;
import io.wispforest.owo.util.SupportsFeaturesImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlDebug;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL32;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class FramebufferWindow extends SupportsFeaturesImpl<WindowContext> implements AutoCloseable, WindowContext {
    private int width;
    private int height;

    private final long handle;

    private Framebuffer framebuffer;
    private int localFramebuffer = 0;

    protected final MinecraftClient client = MinecraftClient.getInstance();

    private final List<NativeResource> disposeList;

    private final EventStream<WindowClosed> windowClosedEvents = WindowClosed.newStream();
    private final EventStream<WindowFramebufferResized> framebufferResizedEvents = WindowFramebufferResized.newStream();
    private final EventStream<WindowMouseMoved> mouseMovedEvents = WindowMouseMoved.newStream();
    private final EventStream<WindowMouseButton> mouseButtonEvents = WindowMouseButton.newStream();
    private final EventStream<WindowMouseScrolled> mouseScrolledEvents = WindowMouseScrolled.newStream();
    private final EventStream<WindowKeyPressed> keyPressedEvents = WindowKeyPressed.newStream();
    private final EventStream<CharTyped> charTypedEvents = CharTyped.newStream();

    public FramebufferWindow(int width, int height, String name, long parentContext) {
        if (glfwGetCurrentContext() != MinecraftClient.getInstance().getWindow().getHandle()) {
            throw new IllegalStateException("Window was created on alternate GL context");
        }

        this.width = width;
        this.height = height;

        try (var ignored = OwoGlUtil.setContext(0)) {
            glfwDefaultWindowHints();
            glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_API);
            glfwWindowHint(GLFW_CONTEXT_CREATION_API, GLFW_NATIVE_CONTEXT_API);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, 1);
            handle = glfwCreateWindow(width, height, name, 0, parentContext);

            if (handle == 0) {
                throw new IllegalStateException("OwoWindow creation failed due to GLFW error");
            }

            glfwMakeContextCurrent(handle);
            glfwSwapInterval(0);
        }

        this.framebuffer = new SimpleFramebuffer(width, height, true, MinecraftClient.IS_SYSTEM_MAC);
        GlDebugUtils.labelObject(GL32.GL_FRAMEBUFFER, this.framebuffer.fbo, "Main context framebuffer for " + this);

        try (var ignored = OwoGlUtil.setContext(this.handle)) {
            GlDebug.enableDebug(client.options.glDebugVerbosity, true);
        }

        initLocalFramebuffer();

        this.disposeList = new ArrayList<>();
        glfwSetWindowCloseCallback(handle, stowAndReturn(GLFWWindowCloseCallback.create(window -> {
            try (var ignored = CurrentWindowContext.setCurrent(this)) {
                windowClosedEvents.sink().onWindowClosed();
            }
        })));
        glfwSetFramebufferSizeCallback(handle, stowAndReturn(GLFWFramebufferSizeCallback.create(this::sizeChanged)));
        glfwSetCursorPosCallback(handle, stowAndReturn(GLFWCursorPosCallback.create((window, xpos, ypos) -> {
            try (var ignored = CurrentWindowContext.setCurrent(this)) {
                mouseMovedEvents.sink().onMouseMoved(xpos, ypos);
            }
        })));
        glfwSetMouseButtonCallback(handle, stowAndReturn(GLFWMouseButtonCallback.create((window, button, action, mods) -> {
            try (var ignored = CurrentWindowContext.setCurrent(this)) {
                mouseButtonEvents.sink().onMouseButton(button, action == GLFW_RELEASE);
            }
        })));
        glfwSetScrollCallback(handle, stowAndReturn(GLFWScrollCallback.create((window, xoffset, yoffset) -> {
            try (var ignored = CurrentWindowContext.setCurrent(this)) {
                mouseScrolledEvents.sink().onMouseScrolled(xoffset, yoffset);
            }
        })));
        glfwSetKeyCallback(handle, stowAndReturn(GLFWKeyCallback.create((window, key, scancode, action, mods) -> {
            try (var ignored = CurrentWindowContext.setCurrent(this)) {
                keyPressedEvents.sink().onKeyPressed(key, scancode, mods, action == GLFW_RELEASE);
            }
        })));
        glfwSetCharModsCallback(handle, stowAndReturn(GLFWCharModsCallback.create((window, codepoint, mods) -> {
            try (var ignored = CurrentWindowContext.setCurrent(this)) {
                charTypedEvents.sink().onCharTyped((char) codepoint, mods);
            }
        })));
    }

    private <T extends NativeResource> T stowAndReturn(T resource) {
        this.disposeList.add(resource);
        return resource;
    }

    public Framebuffer framebuffer() {
        return framebuffer;
    }

    @Override
    public int framebufferWidth() {
        return width;
    }

    @Override
    public int framebufferHeight() {
        return height;
    }

    @Override
    public int scaledWidth() {
        return framebufferWidth();
    }

    @Override
    public int scaledHeight() {
        return framebufferHeight();
    }

    @Override
    public double scaleFactor() {
        return 1;
    }

    @Override
    public long handle() {
        return handle;
    }

    public EventSource<WindowClosed> windowClosed() {
        return windowClosedEvents.source();
    }

    @Override
    public EventSource<WindowFramebufferResized> framebufferResized() {
        return framebufferResizedEvents.source();
    }

    public EventSource<WindowMouseMoved> mouseMoved() {
        return mouseMovedEvents.source();
    }

    public EventSource<WindowMouseButton> mouseButton() {
        return mouseButtonEvents.source();
    }

    public EventSource<WindowMouseScrolled> mouseScrolled() {
        return mouseScrolledEvents.source();
    }

    public EventSource<WindowKeyPressed> keyPressed() {
        return keyPressedEvents.source();
    }

    public EventSource<CharTyped> charTyped() {
        return charTypedEvents.source();
    }

    public void present() {
        if (closed()) return;

        try (var ignored = OwoGlUtil.setContext(handle);
             var ignored1 = GlDebugUtils.pushGroup("Presenting framebuffer of " + this)) {
            // This code intentionally doesn't use Minecraft's RenderSystem
            // class, as it caches GL state that is invalid on this context.
            GL32.glBindFramebuffer(GL32.GL_READ_FRAMEBUFFER, localFramebuffer);
            GL32.glBindFramebuffer(GL32.GL_DRAW_FRAMEBUFFER, 0);

            GL32.glClearColor(1, 1, 1, 1);
            GL32.glClear(GL32.GL_COLOR_BUFFER_BIT | GL32.GL_DEPTH_BUFFER_BIT);
            GL32.glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GL32.GL_COLOR_BUFFER_BIT, GL32.GL_NEAREST);

            // Intentionally doesn't poll events so that all events are on the main window
            RenderSystem.replayQueue();
            Tessellator.getInstance().getBuffer().clear();
            GLFW.glfwSwapBuffers(this.handle);
        }
    }

    private void initLocalFramebuffer() {
        try (var ignored = OwoGlUtil.setContext(this.handle)) {
            if (localFramebuffer != 0) {
                GL32.glDeleteFramebuffers(localFramebuffer);
            }

            this.localFramebuffer = GL32.glGenFramebuffers();
            GL32.glBindFramebuffer(GL32.GL_FRAMEBUFFER, this.localFramebuffer);
            GlDebugUtils.labelObject(GL32.GL_FRAMEBUFFER, this.localFramebuffer, "Local context framebuffer for " + this);
            GL32.glFramebufferTexture2D(GL32.GL_FRAMEBUFFER, GL32.GL_COLOR_ATTACHMENT0, GL32.GL_TEXTURE_2D, this.framebuffer.getColorAttachment(), 0);

            int status = GL32.glCheckFramebufferStatus(GL32.GL_FRAMEBUFFER);
            if (status != GL32.GL_FRAMEBUFFER_COMPLETE)
                throw new IllegalStateException("Failed to create local framebuffer!");
        }
    }

    protected void sizeChanged(long handle, int width, int height) {
        if (framebuffer.viewportWidth == width && framebuffer.viewportHeight == height) return;

        this.width = width;
        this.height = height;

        try (var ignored = OwoGlUtil.setContext(client.getWindow().getHandle())) {
            framebuffer.delete();

            this.framebuffer = new SimpleFramebuffer(width, height, true, MinecraftClient.IS_SYSTEM_MAC);
            GlDebugUtils.labelObject(GL32.GL_FRAMEBUFFER, this.framebuffer.fbo, "Main context framebuffer for " + this);
        }

        initLocalFramebuffer();

        framebufferResizedEvents.sink().onFramebufferResized(width, height);
    }

    public static boolean supportsIcons() {
        int platform = GLFW.glfwGetPlatform();
        return platform == GLFW_PLATFORM_WIN32 || platform == GLFW_PLATFORM_X11;
    }

    public void setIconTextures(ResourceManager manager, List<Identifier> iconIds) {
        if (!supportsIcons()) return;

        List<NativeImage> iconImages = new ArrayList<>(iconIds.size());

        try {
            for (Identifier iconId : iconIds) {
                var icon = manager.getResource(iconId).orElse(null);

                if (icon == null) continue;

                try {
                    iconImages.add(NativeImage.read(icon.getInputStream()));
                } catch (IOException e) {
                    throw new RuntimeException("Couldn't open icon " + iconId, e);
                }
            }

            setIcon(iconImages);
        } finally {
            iconImages.forEach(NativeImage::close);
        }
    }

    public void setIcon(List<NativeImage> icons) {
        if (!supportsIcons()) return;

        List<ByteBuffer> freeList = new ArrayList<>(icons.size());
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            GLFWImage.Buffer buffer = GLFWImage.malloc(icons.size(), memoryStack);

            for (int i = 0; i < icons.size(); i++) {
                NativeImage icon = icons.get(i);
                ByteBuffer imgBuffer = MemoryUtil.memAlloc(icon.getWidth() * icon.getHeight() * 4);
                freeList.add(imgBuffer);
                imgBuffer.asIntBuffer().put(icon.copyPixelsRgba());

                buffer
                    .position(i)
                    .width(icon.getWidth())
                    .height(icon.getHeight())
                    .pixels(imgBuffer);
            }

            GLFW.glfwSetWindowIcon(this.handle, buffer.position(0));
        } finally {
            freeList.forEach(MemoryUtil::memFree);
        }
    }

    public boolean closed() {
        return this.disposeList.isEmpty();
    }

    @Override
    public void close() {
        super.close();

        try (var ignored = OwoGlUtil.setContext(this.handle)) {
            GL32.glDeleteFramebuffers(this.localFramebuffer);
        }

        this.framebuffer.delete();
        glfwDestroyWindow(this.handle);

        this.disposeList.forEach(NativeResource::free);
        this.disposeList.clear();
    }
}
