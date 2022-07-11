package io.wispforest.owo.ui.core;

import com.mojang.blaze3d.platform.GlStateManager;
import io.wispforest.owo.Owo;
import io.wispforest.owo.mixin.ui.ScreenAccessor;
import io.wispforest.owo.ui.util.Drawer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

import java.util.EnumMap;
import java.util.function.BiFunction;

public class OwoUIAdapter<T extends ParentComponent> implements Element, Drawable, Selectable {

    public final T rootComponent;

    public final EnumMap<CursorStyle, Long> cursors;
    protected CursorStyle lastCursorStyle = CursorStyle.POINTER;
    protected boolean disposed = false;

    public final int x, y;
    public final int width, height;

    public boolean enableInspector = false;

    protected OwoUIAdapter(int x, int y, int width, int height, T rootComponent) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.cursors = new EnumMap<>(CursorStyle.class);
        for (var style : CursorStyle.values()) {
            this.cursors.put(style, GLFW.glfwCreateStandardCursor(style.glfw));
        }

        this.rootComponent = rootComponent;
    }

    public static <T extends ParentComponent> OwoUIAdapter<T> create(Screen screen, BiFunction<Sizing, Sizing, T> rootComponentMaker) {
        var rootComponent = rootComponentMaker.apply(Sizing.fill(100), Sizing.fill(100));

        var adapter = new OwoUIAdapter<>(0, 0, screen.width, screen.height, rootComponent);
        ((ScreenAccessor) screen).owo$addDrawableChild(adapter);
        screen.focusOn(adapter);

        return adapter;
    }

    public static <T extends ParentComponent> OwoUIAdapter<T> createWithoutScreen(int x, int y, int width, int height, BiFunction<Sizing, Sizing, T> rootComponentMaker) {
        var rootComponent = rootComponentMaker.apply(Sizing.fill(100), Sizing.fill(100));
        return new OwoUIAdapter<>(x, y, width, height, rootComponent);
    }

    public void inflateAndMount() {
        this.rootComponent.inflate(Size.of(this.width, this.height));
        this.rootComponent.mount(null, this.x, this.y);
    }

    public void dispose() {
        this.cursors.values().forEach(GLFW::glfwDestroyCursor);
        this.disposed = true;
    }

    public boolean toggleInspector() {
        return this.enableInspector = !this.enableInspector;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        final var delta = MinecraftClient.getInstance().getLastFrameDuration();

        this.rootComponent.update(delta, mouseX, mouseY);

        GlStateManager._enableScissorTest();
        this.rootComponent.draw(matrices, mouseX, mouseY, partialTicks, delta);
        GlStateManager._disableScissorTest();

        final var hovered = this.rootComponent.childAt(mouseX, mouseY);
        if (hovered != null && hovered.cursorStyle() != this.lastCursorStyle && !disposed) {
            GLFW.glfwSetCursor(MinecraftClient.getInstance().getWindow().getHandle(), this.cursors.get(hovered.cursorStyle()));
            this.lastCursorStyle = hovered.cursorStyle();
        }

        if (this.enableInspector) {
            Drawer.debug().drawInspector(matrices, this.rootComponent, mouseX, mouseY, true);
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.rootComponent.isInBoundingBox(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.rootComponent.onMouseDown(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return this.rootComponent.onMouseUp(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return this.rootComponent.onMouseScroll(mouseX, mouseY, amount);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return this.rootComponent.onMouseDrag(mouseX, mouseY, deltaX, deltaY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (Owo.DEBUG && keyCode == GLFW.GLFW_KEY_LEFT_SHIFT && (modifiers & GLFW.GLFW_KEY_LEFT_ALT) != 0) {
            this.toggleInspector();
        }

        return this.rootComponent.onKeyPress(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return this.rootComponent.onCharTyped(chr, modifiers);
    }

    @Override
    public SelectionType getType() {
        return SelectionType.NONE;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {}
}
