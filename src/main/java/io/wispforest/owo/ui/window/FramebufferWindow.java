package io.wispforest.owo.ui.window;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.EventStream;
import io.wispforest.owo.util.OwoGlfwUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlDebug;
import net.minecraft.client.gl.SimpleFramebuffer;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL32;
import org.lwjgl.system.NativeResource;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class FramebufferWindow implements AutoCloseable {
    private final int width;
    private final int height;
    private final long handle;
    private final Framebuffer framebuffer;
    private int localFramebuffer = 0;
    protected final MinecraftClient client = MinecraftClient.getInstance();

    private final List<NativeResource> disposeList;

    private final EventStream<WindowClosed> windowClosedEvents = WindowClosed.newStream();
    private final EventStream<WindowResized> windowResizedEvents = WindowResized.newStream();
    private final EventStream<WindowMouseMoved> mouseMovedEvents = WindowMouseMoved.newStream();

    public FramebufferWindow(int width, int height, String name, long parentContext) {
        this.width = width;
        this.height = height;

        try (var ignored = OwoGlfwUtil.setContext(0)) {
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
        }

        this.framebuffer = new SimpleFramebuffer(width, height, true, MinecraftClient.IS_SYSTEM_MAC);

        try (var ignored = OwoGlfwUtil.setContext(this.handle)) {
            GlDebug.enableDebug(client.options.glDebugVerbosity, true);
        }

        initLocalFramebuffer();

        this.disposeList = new ArrayList<>();
        glfwSetWindowCloseCallback(handle, stowAndReturn(GLFWWindowCloseCallback.create(window -> {
            windowClosedEvents.sink().onWindowClosed();
        })));
        glfwSetFramebufferSizeCallback(handle, stowAndReturn(GLFWFramebufferSizeCallback.create(this::sizeChanged)));
        glfwSetCursorPosCallback(handle, stowAndReturn(GLFWCursorPosCallback.create((window, xpos, ypos) -> {
            mouseMovedEvents.sink().onMouseMoved(xpos, ypos);
        })));
        glfwSetMouseButtonCallback(handle, stowAndReturn(GLFWMouseButtonCallback.create((window, button, action, mods) -> {
//            onMouseButton.invoker().onMouseButton(button, action, mods);
        })));
//        disposeList.add(glfwSetScrollCallback(handle, (window, xoffset, yoffset) -> {
//            onMouseScroll.invoker().onMouseScroll(xoffset, yoffset);
//        }));
//        disposeList.add(glfwSetDropCallback(handle, (window, count, names) -> {
//            Path[] paths = new Path[count];
//
//            for (int j = 0; j < count; ++j) {
//                paths[j] = Paths.get(GLFWDropCallback.getName(names, j));
//            }
//
//            onFilesDropped.invoker().onFilesDropped(paths);
//        }));
    }

    private <T extends NativeResource> T stowAndReturn(T resource) {
        this.disposeList.add(resource);
        return resource;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public Framebuffer framebuffer() {
        return framebuffer;
    }

    public EventSource<WindowClosed> windowClosed() {
        return windowClosedEvents.source();
    }

    public EventSource<WindowResized> windowResized() {
        return windowResizedEvents.source();
    }

    public EventSource<WindowMouseMoved> mouseMoved() {
        return mouseMovedEvents.source();
    }

    public void present() {
        try (var ignored = OwoGlfwUtil.setContext(handle)) {
            // This code intentionally doesn't use Minecraft's RenderSystem
            // class, as it caches GL state that is invalid on this context.
            GL32.glBindFramebuffer(GL32.GL_READ_FRAMEBUFFER, localFramebuffer);
            GL32.glBindFramebuffer(GL32.GL_DRAW_FRAMEBUFFER, 0);

            GL32.glClearColor(1, 1, 1, 1);
            GL32.glClear(GL32.GL_COLOR_BUFFER_BIT | GL32.GL_DEPTH_BUFFER_BIT);
            GL32.glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GL32.GL_COLOR_BUFFER_BIT, GL32.GL_NEAREST);

            RenderSystem.flipFrame(handle);
        }
    }

    private void initLocalFramebuffer() {
        if (localFramebuffer != 0) {
            GL32.glDeleteFramebuffers(localFramebuffer);
        }

        this.localFramebuffer = GL32.glGenFramebuffers();
        GL32.glBindFramebuffer(GL32.GL_FRAMEBUFFER, this.localFramebuffer);
        GL32.glFramebufferTexture2D(GL32.GL_FRAMEBUFFER, GL32.GL_COLOR_ATTACHMENT0, GL32.GL_TEXTURE_2D, this.framebuffer.getColorAttachment(), 0);

        int status = GL32.glCheckFramebufferStatus(GL32.GL_FRAMEBUFFER);
        if (status != GL32.GL_FRAMEBUFFER_COMPLETE)
            throw new IllegalStateException("Failed to create local framebuffer!");
    }

    protected void sizeChanged(long handle, int width, int height) {
        if (framebuffer.viewportWidth == width && framebuffer.viewportHeight == height) return;

        framebuffer.resize(width, height, MinecraftClient.IS_SYSTEM_MAC);
        initLocalFramebuffer();

        windowResizedEvents.sink().onWindowResized(width, height);
    }

    public boolean closed() {
        return this.disposeList.isEmpty();
    }

    @Override
    public void close() {
        this.disposeList.forEach(NativeResource::free);
        this.disposeList.clear();
        glfwDestroyWindow(this.handle);
    }
}
