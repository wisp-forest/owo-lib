package io.wispforest.owo.ui;

import io.wispforest.owo.Owo;
import io.wispforest.owo.ui.definitions.ParentComponent;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.function.BiFunction;

/**
 * A minimal implementation of a Screen which fully
 * supports all aspects of the UI system. Implementing this class
 * is trivial, as you only need to provide implementations for
 * {@link #createAdapter()} to initialize the UI system and {@link #build(ParentComponent)}
 * which is where you declare your component hierarchy.
 * <p>
 * Should you be locked into a different superclass on your screen already,
 * you can easily copy all code from this class into your screen - as you
 * can see supporting the entire feature-set of owo-ui only requires
 * very few changes to how a vanilla screen works
 *
 * @param <R> The type of root component this screen uses
 */
public abstract class BaseOwoScreen<R extends ParentComponent> extends Screen {

    /**
     * The UI adapter of this screen. This handles
     * all user input as well as setting up GL state for rendering
     * and managing component focus
     */
    protected OwoUIAdapter<R> uiAdapter = null;

    protected BaseOwoScreen(Text title) {
        super(title);
    }

    protected BaseOwoScreen() {
        this(Text.empty());
    }

    /**
     * Initialize the UI adapter for this screen. Usually
     * the body of this method will simply consist of a call
     * to {@link OwoUIAdapter#create(Screen, BiFunction)}
     *
     * @return The UI adapter for this screen to use
     */
    protected abstract OwoUIAdapter<R> createAdapter();

    /**
     * Build the component hierarchy of this screen,
     * called after the adapter and root component have been
     * initialized by {@link #createAdapter()}
     *
     * @param rootComponent The root component created
     *                      in the previous initialization step
     */
    protected abstract void build(R rootComponent);

    @Override
    protected void init() {
        if (this.uiAdapter != null) {
            this.uiAdapter.dispose();
        }

        this.uiAdapter = this.createAdapter();
        try {
            this.build(this.uiAdapter.rootComponent);
        } catch (Exception error) {
            Owo.LOGGER.warn("Could not build owo screen", error);
        }

        this.uiAdapter.inflateAndMount();
        this.client.keyboard.setRepeatEvents(true);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && this.shouldCloseOnEsc()) {
            this.close();
            return true;
        }

        return this.uiAdapter.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return this.uiAdapter.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Nullable
    @Override
    public Element getFocused() {
        return this.uiAdapter;
    }

    @Override
    public void removed() {
        this.uiAdapter.dispose();
        this.client.keyboard.setRepeatEvents(false);
    }
}
