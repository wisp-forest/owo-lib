package io.wispforest.owo.ui.core;

import com.mojang.blaze3d.platform.GlStateManager;
import io.wispforest.owo.Owo;
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

/**
 * A UI adapter constitutes the main entrypoint to using owo-ui.
 * It takes care of rendering the UI tree correctly, handles input events
 * and cursor styling as well as the component inspector.
 * <p>
 * Additionally, the adapter implements all interfaces required for it
 * to be treated as a normal widget by the vanilla screen system - this means
 * even if you choose to not use {@link io.wispforest.owo.ui.base.BaseOwoScreen}
 * you can always simply add it as a widget and get most of the functionality
 * working out of the box
 *
 * @see io.wispforest.owo.ui.base.BaseOwoScreen
 */
public class OwoUIAdapter<T extends ParentComponent> implements Element, Drawable, Selectable {

    public final T rootComponent;

    public final EnumMap<CursorStyle, Long> cursors = new EnumMap<>(CursorStyle.class);
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

        for (var style : CursorStyle.values()) {
            this.cursors.put(style, GLFW.glfwCreateStandardCursor(style.glfw));
        }

        this.rootComponent = rootComponent;
    }

    /**
     * Create a UI adapter for the given screen. This also sets it up
     * to be rendered and receive input events, without needing you to
     * do any more setup
     *
     * @param screen             The screen for which to create an adapter
     * @param rootComponentMaker A function which will create the root component of this screen
     * @param <T>                The type of root component the created adapter will use
     * @return The new UI adapter, already set up for the given screen
     */
    public static <T extends ParentComponent> OwoUIAdapter<T> create(Screen screen, BiFunction<Sizing, Sizing, T> rootComponentMaker) {
        var rootComponent = rootComponentMaker.apply(Sizing.fill(100), Sizing.fill(100));

        var adapter = new OwoUIAdapter<>(0, 0, screen.width, screen.height, rootComponent);
        screen.addDrawableChild(adapter);
        screen.focusOn(adapter);

        return adapter;
    }

    /**
     * Create a new UI adapter without the specific context of a screen - use this
     * method when you want to embed owo-ui into a different context
     *
     * @param x                  The x-coordinate of the top-left corner of the root component
     * @param y                  The y-coordinate of the top-left corner of the root component
     * @param width              The width of the available area, in pixels
     * @param height             The height of the available area, in pixels
     * @param rootComponentMaker A function which will create the root component of the adapter
     * @param <T>                The type of root component the created adapter will use
     * @return The new UI adapter, ready for layout inflation
     */
    public static <T extends ParentComponent> OwoUIAdapter<T> createWithoutScreen(int x, int y, int width, int height, BiFunction<Sizing, Sizing, T> rootComponentMaker) {
        var rootComponent = rootComponentMaker.apply(Sizing.fill(100), Sizing.fill(100));
        return new OwoUIAdapter<>(x, y, width, height, rootComponent);
    }

    /**
     * Begin the layout process of the UI tree and
     * mount the tree once the layout is inflated
     * <p>
     * After this method has executed, this adapter is ready for rendering
     */
    public void inflateAndMount() {
        this.rootComponent.inflate(Size.of(this.width, this.height));
        this.rootComponent.mount(null, this.x, this.y);
    }

    /**
     * Dispose this UI adapter - this will destroy the cursor
     * objects held onto by this adapter and stop updating the cursor style
     * <p>
     * After this method has executed, this adapter can safely be garbage-collected
     */
    public void dispose() {
        this.cursors.values().forEach(GLFW::glfwDestroyCursor);
        this.disposed = true;
    }

    /**
     * @return Toggle rendering of the inspector
     */
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

        this.rootComponent.drawTooltip(matrices, mouseX, mouseY, partialTicks, delta);

        final var hovered = this.rootComponent.childAt(mouseX, mouseY);
        if (!disposed && hovered != null && hovered.cursorStyle() != this.lastCursorStyle) {
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