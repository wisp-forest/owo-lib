package io.wispforest.owo.ui.window;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.util.OwoGlUtil;
import io.wispforest.owo.ui.window.context.CurrentWindowContext;
import io.wispforest.owo.ui.window.context.WindowContext;
import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.EventStream;
import io.wispforest.owo.util.SupportsFeaturesImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlDebug;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Pair;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL32;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwSetCharModsCallback;

public abstract class OwoWindow<R extends ParentComponent> extends SupportsFeaturesImpl<WindowContext> implements WindowContext {
    private String title = "owo-ui window";
    private int screenWidth = 854;
    private int screenHeight = 480;
    private WindowIcon icon = null;
    private final List<Pair<Integer, Integer>> windowHints = new ArrayList<>();

    private int framebufferWidth;
    private int framebufferHeight;

    private long handle = 0;
    private Framebuffer framebuffer;
    private int localFramebuffer = 0;
    private final List<NativeResource> disposeList = new ArrayList<>();
    private final MinecraftClient client = MinecraftClient.getInstance();

    private int scaleFactor;
    private int scaledWidth;
    private int scaledHeight;
    protected OwoUIAdapter<R> uiAdapter = null;

    private int mouseX = -1;
    private int mouseY = -1;
    private int deltaX = 0;
    private int deltaY = 0;
    private int activeButton = -1;

    private final EventStream<WindowFramebufferResized> framebufferResizedEvents = WindowFramebufferResized.newStream();

    public OwoWindow() {

    }

    public OwoWindow<R> size(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        if (this.handle != 0) {
            glfwSetWindowSize(this.handle, screenWidth, screenHeight);
        }
        
        return this;
    }

    public OwoWindow<R> title(String title) {
        this.title = title;

        if (this.handle != 0) {
            glfwSetWindowTitle(this.handle, title);
        }

        return this;
    }

    public OwoWindow<R> icon(WindowIcon icon) {
        this.icon = icon;

        if (this.handle != 0) {
            applyIcon();
        }

        return this;
    }

    public OwoWindow<R> windowHint(int hint, int value) {
        if (this.handle != 0) {
            throw new IllegalStateException("Tried to add window hint after window was opened");
        }

        windowHints.add(new Pair<>(hint, value));

        return this;
    }

    public void open() {
        try (var ignored = OwoGlUtil.setContext(0)) {
            glfwDefaultWindowHints();
            glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_API);
            glfwWindowHint(GLFW_CONTEXT_CREATION_API, GLFW_NATIVE_CONTEXT_API);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, 1);

            for (var hint : windowHints) {
                glfwWindowHint(hint.getLeft(), hint.getRight());
            }

            this.handle = glfwCreateWindow(this.screenWidth, this.screenHeight, this.title, 0, MinecraftClient.getInstance().getWindow().getHandle());

            if (this.handle == 0) {
                throw new IllegalStateException("OwoWindow creation failed due to GLFW error");
            }

            glfwMakeContextCurrent(this.handle);
            glfwSwapInterval(0);
        }

        applyIcon();

        int[] framebufferWidthArr = new int[1];
        int[] framebufferHeightArr = new int[1];
        glfwGetFramebufferSize(this.handle, framebufferWidthArr, framebufferHeightArr);
        this.framebufferWidth = framebufferWidthArr[0];
        this.framebufferHeight = framebufferHeightArr[0];

        this.framebuffer = new SimpleFramebuffer(this.framebufferWidth, this.framebufferHeight, true, MinecraftClient.IS_SYSTEM_MAC);

        try (var ignored = OwoGlUtil.setContext(this.handle)) {
            GlDebug.enableDebug(client.options.glDebugVerbosity, true);
        }

        initLocalFramebuffer();

        glfwSetWindowCloseCallback(handle, stowAndReturn(GLFWWindowCloseCallback.create(window -> {
            try (var ignored = CurrentWindowContext.setCurrent(this)) {
                this.close();
            }
        })));

        glfwSetWindowSizeCallback(handle, stowAndReturn(GLFWWindowSizeCallback.create((window, width, height) -> {
            this.screenWidth = width;
            this.screenHeight = height;
        })));

        glfwSetFramebufferSizeCallback(handle, stowAndReturn(GLFWFramebufferSizeCallback.create((window, width, height) -> {
            if (this.framebufferWidth == width && this.framebufferHeight == height) return;

            this.framebufferWidth = width;
            this.framebufferHeight = height;

            try (var ignored = OwoGlUtil.setContext(client.getWindow().getHandle())) {
                framebuffer.delete();

                this.framebuffer = new SimpleFramebuffer(width, height, true, MinecraftClient.IS_SYSTEM_MAC);
            }

            initLocalFramebuffer();

            recalculateScale();

            try (var ignored = CurrentWindowContext.setCurrent(this)) {
                uiAdapter.moveAndResize(0, 0, scaledWidth(), scaledHeight());

                this.framebufferResizedEvents.sink().onFramebufferResized(width, height);
            }
        })));

        glfwSetCursorPosCallback(handle, stowAndReturn(GLFWCursorPosCallback.create((window, xpos, ypos) -> {
            int newX = (int) (xpos / scaleFactor);
            int newY = (int) (ypos / scaleFactor);

            deltaX += newX - mouseX;
            deltaY += newY - mouseY;

            mouseY = newY;
            mouseX = newX;
        })));

        glfwSetMouseButtonCallback(handle, stowAndReturn(GLFWMouseButtonCallback.create((window, button, action, mods) -> {
            try (var ignored = CurrentWindowContext.setCurrent(this)) {
                if (action == GLFW_RELEASE) {
                    this.activeButton = -1;

                    uiAdapter.mouseReleased(mouseX, mouseY, button);
                } else {
                    this.activeButton = button;

                    uiAdapter.mouseClicked(mouseX, mouseY, button);
                }
            }
        })));

        glfwSetScrollCallback(handle, stowAndReturn(GLFWScrollCallback.create((window, xoffset, yoffset) -> {
            try (var ignored = CurrentWindowContext.setCurrent(this)) {
                double yAmount = (client.options.getDiscreteMouseScroll().getValue() ? Math.signum(yoffset) : yoffset)
                    * client.options.getMouseWheelSensitivity().getValue();
                double xAmount = (client.options.getDiscreteMouseScroll().getValue() ? Math.signum(xoffset) : xoffset)
                    * client.options.getMouseWheelSensitivity().getValue();
                uiAdapter.mouseScrolled(mouseX, mouseY, xAmount, yAmount);
            }
        })));

        glfwSetKeyCallback(handle, stowAndReturn(GLFWKeyCallback.create((window, key, scancode, action, mods) -> {
            try (var ignored = CurrentWindowContext.setCurrent(this)) {
                if (action == GLFW_RELEASE) {
                    uiAdapter.keyReleased(key, scancode, mods);
                } else {
                    uiAdapter.keyPressed(key, scancode, mods);
                }
            }
        })));

        glfwSetCharModsCallback(handle, stowAndReturn(GLFWCharModsCallback.create((window, codepoint, mods) -> {
            try (var ignored = CurrentWindowContext.setCurrent(this)) {
                uiAdapter.charTyped((char) codepoint, mods);
            }
        })));

        recalculateScale();

        try (var ignored = CurrentWindowContext.setCurrent(this)) {
            this.uiAdapter = createAdapter();
            build(this.uiAdapter.rootComponent);
            this.uiAdapter.inflateAndMount();
        }

        OpenWindows.add(this);
    }

    private <T extends NativeResource> T stowAndReturn(T resource) {
        this.disposeList.add(resource);
        return resource;
    }

    private void applyIcon() {
        if (icon == null) return;

        List<NativeImage> icons = icon.listIconImages();

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

            if (icon.closeAfterUse())
                icons.forEach(NativeImage::close);
        }
    }

    private void initLocalFramebuffer() {
        try (var ignored = OwoGlUtil.setContext(this.handle)) {
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
    }

    protected abstract OwoUIAdapter<R> createAdapter();

    protected abstract void build(R rootComponent);

    public void recalculateScale() {
        int guiScale = MinecraftClient.getInstance().options.getGuiScale().getValue();
        boolean forceUnicodeFont = MinecraftClient.getInstance().options.getForceUnicodeFont().getValue();

        int factor = 1;

        while (
                factor != guiScale
                        && factor < this.framebufferWidth()
                        && factor < this.framebufferHeight()
                        && this.framebufferWidth() / (factor + 1) >= 320
                        && this.framebufferHeight() / (factor + 1) >= 240
        ) {
            ++factor;
        }

        if (forceUnicodeFont && factor % 2 != 0) {
            ++factor;
        }

        this.scaleFactor = factor;
        this.scaledWidth = (int) Math.ceil((double) this.framebufferWidth() / scaleFactor);
        this.scaledHeight = (int) Math.ceil((double) this.framebufferHeight() / scaleFactor);
    }

    private void tickMouse() {
        if (deltaX == 0 && this.deltaY == 0) return;

        uiAdapter.mouseMoved(mouseX, mouseY);

        if (activeButton != -1) uiAdapter.mouseDragged(mouseX, mouseY, activeButton, deltaX, deltaY);

        deltaX = 0;
        deltaY = 0;
    }

    public void render() {
        if (closed()) return;

        try (var ignored = CurrentWindowContext.setCurrent(this)) {
            tickMouse();

            framebuffer().beginWrite(true);

            RenderSystem.clearColor(0, 0, 0, 0);
            RenderSystem.clear(GL32.GL_COLOR_BUFFER_BIT | GL32.GL_DEPTH_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);

            Matrix4f matrix4f = new Matrix4f()
                    .setOrtho(
                            0.0F,
                            scaledWidth(),
                            scaledHeight(),
                            0.0F,
                            1000.0F,
                            21000.0F
                    );

            try (var ignored2 = OwoGlUtil.setProjectionMatrix(matrix4f, VertexSorter.BY_Z)) {
                Matrix4fStack matrixStack = RenderSystem.getModelViewStack();
                matrixStack.pushMatrix();
                matrixStack.identity();
                matrixStack.translate(0.0F, 0.0F, -11000.0F);
                RenderSystem.applyModelViewMatrix();
                DiffuseLighting.enableGuiDepthLighting();

                var consumers = client.getBufferBuilders().getEntityVertexConsumers();
                uiAdapter.render(new DrawContext(client, consumers), mouseX, mouseY, client.getRenderTickCounter().getTickDelta(false));
                consumers.draw();

                RenderSystem.getModelViewStack().popMatrix();
                RenderSystem.applyModelViewMatrix();
            }

            framebuffer.endWrite();
        }

        try (var ignored = OwoGlUtil.setContext(handle)) {
            // This code intentionally doesn't use Minecraft's RenderSystem
            // class, as it caches GL state that is invalid on this context.
            GL32.glBindFramebuffer(GL32.GL_READ_FRAMEBUFFER, localFramebuffer);
            GL32.glBindFramebuffer(GL32.GL_DRAW_FRAMEBUFFER, 0);

            GL32.glClearColor(1, 1, 1, 1);
            GL32.glClear(GL32.GL_COLOR_BUFFER_BIT | GL32.GL_DEPTH_BUFFER_BIT);
            GL32.glBlitFramebuffer(0, 0, this.framebufferWidth, this.framebufferHeight, 0, 0, this.framebufferWidth, this.framebufferHeight, GL32.GL_COLOR_BUFFER_BIT, GL32.GL_NEAREST);

            // Intentionally doesn't poll events so that all events are on the main window
            Tessellator.getInstance().clear();
            GLFW.glfwSwapBuffers(this.handle);
        }
    }

    @Override
    public long handle() {
        return handle;
    }

    @Override
    public EventSource<WindowFramebufferResized> framebufferResized() {
        return framebufferResizedEvents.source();
    }

    @Override
    public Framebuffer framebuffer() {
        return framebuffer;
    }

    @Override
    public int framebufferWidth() {
        return framebufferWidth;
    }

    @Override
    public int framebufferHeight() {
        return framebufferHeight;
    }

    @Override
    public double scaleFactor() {
        return scaleFactor;
    }

    @Override
    public int scaledWidth() {
        return scaledWidth;
    }

    @Override
    public int scaledHeight() {
        return scaledHeight;
    }

    public boolean closed() {
        return this.handle == 0;
    }

    public void close() {
        this.destroyFeatures();
        uiAdapter.dispose();
        OpenWindows.remove(this);

        try (var ignored = OwoGlUtil.setContext(this.handle)) {
            GL32.glDeleteFramebuffers(this.localFramebuffer);
        }

        this.framebuffer.delete();
        glfwDestroyWindow(this.handle);
        this.handle = 0;

        this.disposeList.forEach(NativeResource::free);
        this.disposeList.clear();
    }
}
